package org.uge.greed.execution;

import fr.uge.ugegreed.Checker;
import org.uge.greed.commands.StartCommand;
import org.uge.greed.greed.UGEGreed;
import org.uge.greed.messaging.messages.UGEGreedWorkloadRequest;
import org.uge.greed.messaging.messages.UGEGreedWorkloadResult;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;



public class UGEGreddExcutionService {
    private static final String JAR_FILE_NAME = "checker.jar";
    private final Object lock = new Object();
    private final ArrayBlockingQueue<UGEGreedWorkloadResult> workloadResults = new ArrayBlockingQueue<>(1000 * 1000);


    private class ScriptLoader {
        private static final Logger logger = Logger.getLogger(ScriptLoader.class.getName());
        private void downloadJarFile() {
            new Thread(() -> {
                synchronized (lock) {
                    setState(RUNNER_STATE.DOWNLOADING);
                    try {
                        URLConnection connection = commandeToExecute.getUrlJar().openConnection();
                        InputStream inputStream = connection.getInputStream();

                        File file = new File(JAR_FILE_NAME);
                        if (file.exists()) {
                            file.delete();
                        }

                        FileOutputStream outputStream = new FileOutputStream(JAR_FILE_NAME);

                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;

                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        setState(RUNNER_STATE.READY_TO_RUN);
                    } catch (IOException e) {
                        logger.info("Failed to download the jar file from %s".format(commandeToExecute.getUrlJar().toString()));
                        setState(RUNNER_STATE.AVAILABLE);
                    }
                }
            }
            ).start();
        }


        /**
         * This method downloads the jar file from the given url
         * and creates an instance of the class assuming it implements the
         * fr.uge.ugegreed.Checker interface.
         * <p>
         * This method can both be used retrieve the class from a local jar file
         * or from a jar file provided by an HTTP server. The behavior depends
         * on the url parameter.
         *
         * @param url       the url of the jar file
         * @param className the fully qualified name of the class to load
         * @return an instance of the class if it exists
         */
        public static Optional<Checker> retrieveCheckerFromURL(URL url, String className) {
            Objects.requireNonNull(url);
            Objects.requireNonNull(className);
            var urls = new URL[]{url};
            System.out.println(url);
            var urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
            try {
                var clazz = Class.forName(className, true, urlClassLoader);
                var constructor = clazz.getDeclaredConstructor();
                Object instance = constructor.newInstance();
                return Optional.of((Checker) instance);
            } catch (ClassNotFoundException e) {
                logger.info("The class %s was not found in %s. The jarfile might not be present at the given URL.".format(className, url));
                e.printStackTrace();
                return Optional.empty();
            } catch (NoSuchMethodException e) {
                logger.info("Class %s in jar %s cannot be cast to fr.uge.ugegreed.Checker".format(className, url));
                return Optional.empty();
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                logger.info("Failed to create an instance of %s".format(className));
                System.out.println("Returning empty optional IllegalAccessException ");
                return Optional.empty();
            }catch (NoSuchElementException ex){
                logger.info(ex.toString());
                return Optional.empty();
            }
        }

        public static Optional<Checker> checkerFromDisk(Path jarPath, String className) {
            try {
                URL url = jarPath.toUri().toURL();
                logger.info("Loading class %s from %s".format(className, url));
                return  retrieveCheckerFromURL(url, className);
            } catch (MalformedURLException e) {
                return Optional.empty();
            }
        }


        public HashMap<Long, String> excuteFunction(long startValue, long endValue, String fullyQualifiedName) throws InterruptedException {
            Checker checker = checkerFromDisk(Path.of(JAR_FILE_NAME), fullyQualifiedName).orElseThrow();
            HashMap<Long, String> results = new HashMap<>();

            for (long j = startValue; j <= endValue; j++) {
                String res = checker.check(j);
                results.put(j,res);
            }

            return results;
        }
    }

    private enum RUNNER_STATE {
        RUNNING,
        DOWNLOADING,
        READY_TO_RUN,
        AVAILABLE
    }

    private UGEGreed mainService;
    private ScriptLoader scriptLoader;
    private UGEGreedWorkloadRequest currentRequest;

    private RUNNER_STATE state = RUNNER_STATE.AVAILABLE;
    private StartCommand commandeToExecute;
    private ServerConfig resultDestination;
    private int currentCommandeWorkloadId ;

    public UGEGreddExcutionService(UGEGreed mainService) {
        this.mainService = mainService;
        this.scriptLoader = new ScriptLoader();
    }

    public void setState(RUNNER_STATE state) {
        this.state = state;
    }

    public boolean isBusy() {
        return state != RUNNER_STATE.AVAILABLE;
    }

    public boolean setCommande(StartCommand command, ServerConfig resultDestination,int currentCommandeWorkloadId, UGEGreedWorkloadRequest request) {
        if (this.isBusy()) {
            return false;
        }
        this.commandeToExecute = command ;
        this.resultDestination = resultDestination ;
        this.scriptLoader.downloadJarFile() ;
        this.currentCommandeWorkloadId = currentCommandeWorkloadId ;
        this.currentRequest = request ;
        return true;
    }

    public void checkAndExecute() {
        if (this.state == RUNNER_STATE.READY_TO_RUN) {
            new Thread(() -> {
                synchronized (lock) {
                    try {
                        this.setState(RUNNER_STATE.RUNNING);
                        HashMap<Long,String> results = this.scriptLoader.excuteFunction(commandeToExecute.getStartRange(), commandeToExecute.getEndRange(), commandeToExecute.getFullyQualifiedName());
                        for (Map.Entry<Long, String> entry : results.entrySet()) {
                            Long calculatedValue = entry.getKey();
                            String Results = entry.getValue();
                            UGEGreedWorkloadResult workloadRequest = this.mainService.getMessagingService().createWorkloadRequestMessage(this.resultDestination,
                                    this.currentCommandeWorkloadId,
                                    Results,
                                    calculatedValue);
                            workloadResults.add(workloadRequest);
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("ERR: dans l'execution du task. envoie d'un message NACK");
                        this.mainService.getMessagingService().sendWorkloadNAckMessage(
                                this.currentRequest.getSender(),
                                this.currentRequest.getWorkloadId()
                        );
                    }
                }
            }).start();
        }
    }

    public void sendIfWorkloadIsDone() {
        if(this.state == RUNNER_STATE.RUNNING){
            if(workloadResults.size() == (commandeToExecute.getEndRange() - commandeToExecute.getStartRange() + 1)){
                while (!this.workloadResults.isEmpty()){
                    UGEGreedWorkloadResult workloadResult = this.workloadResults.poll();
                    this.mainService.getMessagingService().sendWorkloadResultMessage(workloadResult);
                }
                this.setState(RUNNER_STATE.AVAILABLE);
            }
        }
    }
}
