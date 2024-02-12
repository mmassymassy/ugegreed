package org.uge.greed.workloadRequest;

import org.uge.greed.commands.StartCommand;
import org.uge.greed.greed.UGEGreed;
import org.uge.greed.messaging.messages.UGEGreedWorkloadRequest;
import org.uge.greed.network.tcp.client.Client;
import org.uge.greed.network.tcp.server.ServerConfig;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

public class UGEGreedWorkloadRequestService {
    private static Logger logger = Logger.getLogger(Client.class.getName());
    private Object lock = new Object();

    public enum UGEGreedWorkloadRequestStatus {
        REQUESTED,
        ACK,
        NACK,
        DONE
    }

    private final HashMap<String, UGEGreedWorkloadRequestStatus> pairsResponses = new HashMap();
    private final HashMap<Integer, UGEGreedWorkloadRequest> workloadRequests = new HashMap<>();
    private final HashMap<String, UGEGreedWorkloadRequest> workloadDistribution = new HashMap<>();
    private final HashMap<UGEGreedWorkloadRequest, Boolean> workloadRequestsStatus = new HashMap<>();
    private StartCommand currentCommande;
    private final HashMap<Long, String> workloadResults = new HashMap<>();
    private UGEGreed mainService;
    private ArrayBlockingQueue<ArrayList<UGEGreedWorkloadRequest>> workloadRequestsToDistribute = new ArrayBlockingQueue<>(1000);

    private ArrayList<UGEGreedWorkloadRequest> currentWorkloadRequestBatch;

    public static int TASK_LIMIT_PER_WORKER = 100;

    public UGEGreedWorkloadRequestService(UGEGreed mainService) {
        this.mainService = mainService;
    }

    public UGEGreed getMainService() {
        return this.mainService;
    }


    public void addWorkloadResult(Long value, String result) {
        this.workloadResults.put(value, result);
    }

    public UGEGreedWorkloadRequest getWorkloadRequest(int workloadId) {
        return this.workloadRequests.get(workloadId);
    }

    private boolean checkIfAllResultsArePresent(long start, long end) {
        for (long i = start; i <= end; i++) {
            if (!this.workloadResults.containsKey(i)) {
                return false;
            }
        }
        return true;
    }

    private Boolean checkIfWorkloadInCurrentBatchAreDone() {
        if (this.currentWorkloadRequestBatch == null) {
            return true;
        }
        for (UGEGreedWorkloadRequest request : currentWorkloadRequestBatch) {
            if (!workloadRequestsStatus.get(request)) {
                return false;
            }
        }

        this.currentWorkloadRequestBatch = null;
        return true;
    }

    public Boolean checkIfWorkloadRequestIsFullfilled(int workloadId) {
        if ((workloadRequestsStatus.containsKey(workloadId)) && workloadRequestsStatus.get(getWorkloadRequest(workloadId))) {
            return true;
        } else {
            UGEGreedWorkloadRequest request = this.getWorkloadRequest(workloadId);
            if (request == null) {
                return false;
            }
            // get the size of the request from start and end value
            boolean isDone = checkIfAllResultsArePresent(request.getStartValue(), request.getEndValue());
            if (isDone) {
                workloadRequestsStatus.put(request, true);
            }
            return isDone;
        }
    }

    public ArrayList<ArrayList<UGEGreedWorkloadRequest>> batchWorkloadRequestWithMaxStep() {
        ArrayList<ArrayList<UGEGreedWorkloadRequest>> batched = new ArrayList<>();


        int i = 0;

        System.out.println("info: Distribution des taches entre " + this.mainService.getPairs().size() + " noeuds avec " + TASK_LIMIT_PER_WORKER + " calcule pour chaque un.");

        Long start = this.currentCommande.getStartRange() + (i * TASK_LIMIT_PER_WORKER);
        Long end = 0L;

        while (true) {
            Iterator<ServerConfig> iter = this.mainService.getPairs().iterator();
            ArrayList<UGEGreedWorkloadRequest> batch = new ArrayList<>();
            while (iter.hasNext()) {
                if (!batched.isEmpty()) {
                    start = end;
                }

                end = start + TASK_LIMIT_PER_WORKER;

                if (end > currentCommande.getEndRange()) {
                    end = this.currentCommande.getEndRange();
                }

                ServerConfig pair = iter.next();
                UGEGreedWorkloadRequest message = this.mainService.getMessagingService().createWorkloadRequestMessage(
                        pair,
                        i,
                        start,
                        end,
                        this.currentCommande.getFullyQualifiedName(),
                        this.currentCommande.getUrlJar()
                );

                batch.add(message);
                this.workloadRequests.put(i, message);
                this.workloadRequestsStatus.put(message, false);
                this.workloadDistribution.put(pair.getAppId(), message);
                this.pairsResponses.put(pair.getAppId(), UGEGreedWorkloadRequestStatus.REQUESTED);
                i++;

                if (end == this.currentCommande.getEndRange()) {
                    break;
                }
            }
            batched.add(batch);
            if (end == this.currentCommande.getEndRange()) {
                break;
            }
        }
        return batched;
    }

    public ArrayList<ArrayList<UGEGreedWorkloadRequest>> batchWorkloadRequestWithStep(int step) {
        ArrayList<UGEGreedWorkloadRequest> batch = new ArrayList<>();
        ArrayList<ArrayList<UGEGreedWorkloadRequest>> batched = new ArrayList<>();


        Iterator<ServerConfig> iter = this.mainService.getPairs().iterator();
        int i = 0;

        System.out.println("info: Distribution des taches entre " + this.mainService.getPairs().size() + " noeuds avec " + step + " calcule pour chaque un.");
        while (iter.hasNext()) {
            Long start = this.currentCommande.getStartRange() + (i * step);
            Long end = start + step;

            if (i == this.mainService.getPairs().size() - 1) {
                end = this.currentCommande.getEndRange();
            }
            ServerConfig pair = iter.next();
            UGEGreedWorkloadRequest message = this.mainService.getMessagingService().createWorkloadRequestMessage(
                    pair,
                    i,
                    start,
                    end,
                    this.currentCommande.getFullyQualifiedName(),
                    this.currentCommande.getUrlJar()
            );


            batch.add(message);
            this.workloadRequests.put(i, message);
            this.workloadRequestsStatus.put(message, false);
            this.workloadDistribution.put(pair.getAppId(), message);
            this.pairsResponses.put(pair.getAppId(), UGEGreedWorkloadRequestStatus.REQUESTED);
            i++;
        }

        batched.add(batch);
        return batched;
    }

    public void distributeWorkload(StartCommand commande) {
        if (commande == null) {
            return;
        }
        this.currentCommande = commande;

        this.mainService.setState(UGEGreed.UGEGReedStat.HAS_SENT_WORKLOAD);
        workloadResults.clear();

        System.out.println("-- WORKLOAD DISTRIBUTION --");
        if (this.mainService.getPairs().isEmpty()) {
            System.out.println("err: Il n'ya pas des noeud dans le reseaux");
            System.out.println("-- WORKLOAD DISTRIBUTION ERROR --");
        }

        Long range = commande.getEndRange() - commande.getStartRange();
        // TODO fix devision by zero ( if no peers just run the task locally )

        Long step = range / this.mainService.getPairs().size();


        ArrayList<ArrayList<UGEGreedWorkloadRequest>> requests = null;

        if (step > TASK_LIMIT_PER_WORKER) {
            requests = this.batchWorkloadRequestWithMaxStep();
        } else {
            requests = this.batchWorkloadRequestWithStep(step.intValue());
        }

        for (ArrayList<UGEGreedWorkloadRequest> batch : requests) {
            workloadRequestsToDistribute.add(batch);
        }

        System.out.println("-- WORKLOAD DISTRIBUTION DONE --");
    }

    public void clearTaskAfterFinish() {
        this.workloadResults.clear();
        this.pairsResponses.clear();
        this.currentCommande = null;
        this.workloadDistribution.clear();
    }

    public void setWorkloadRequestResponseForPeerASAck(String peerId) {
        this.pairsResponses.put(peerId, UGEGreedWorkloadRequestStatus.ACK);
    }

    public void setWorkloadRequestResponseForPeerASNAck(String peerId) {
        this.pairsResponses.put(peerId, UGEGreedWorkloadRequestStatus.NACK);
    }

    public void setWorkloadRequestResponseForPeerASDone(String peerId) {
        this.pairsResponses.put(peerId, UGEGreedWorkloadRequestStatus.DONE);
    }

    private ArrayList<String> getNackResponsesNumber() {
        ArrayList<String> list = new ArrayList();
        for (Map.Entry<String, UGEGreedWorkloadRequestStatus> pairRes :
                pairsResponses.entrySet()) {

            if (pairRes.getValue() == UGEGreedWorkloadRequestStatus.NACK) {
                list.add(pairRes.getKey());
            }
        }
        return list;
    }

    private String getFirstDonePair() {
        for (Map.Entry<String, UGEGreedWorkloadRequestStatus> pairRes :
                pairsResponses.entrySet()) {

            if (pairRes.getValue() == UGEGreedWorkloadRequestStatus.DONE) {
                return pairRes.getKey();
            }
        }
        return null;
    }

    private void writeAndFinish() {
        new Thread(() -> {
            synchronized (lock) {
                try {
                    if (currentCommande == null) {
                        return;
                    }
                    // imprimer un message dans le log pour informer que le job a terminer dans tous les noeud
                    System.out.println("-- WORKLOAD FINISHED --");
                    System.out.println("printing results to the file : " + currentCommande.getFilename());

                    BufferedWriter writer = new BufferedWriter(new FileWriter(currentCommande.getFilename()));
                    for (Map.Entry<Long, String> entry : this.workloadResults.entrySet()) {
                        writer.write(entry.getValue());
                        writer.newLine();
                    }
                    writer.close();

                    System.out.println("printing to : " + currentCommande.getFilename() + " is done.");
                    System.out.println("-- WORKLOAD FINISHED --");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clearTaskAfterFinish();
            }
        }).start();
    }

    public void sendWorkloadRequestBatch() {
        if (this.workloadRequestsToDistribute.size() == 0) {
            return;
        }


        if (this.currentWorkloadRequestBatch == null) {
            this.currentWorkloadRequestBatch = this.workloadRequestsToDistribute.poll();
            for (UGEGreedWorkloadRequest request : this.currentWorkloadRequestBatch) {
                this.mainService.getMessagingService().sendWorkloadRequestMessage(request);
            }
        } else {
            if (checkIfWorkloadInCurrentBatchAreDone()) {
                this.currentWorkloadRequestBatch = this.workloadRequestsToDistribute.poll();
                for (UGEGreedWorkloadRequest request : this.currentWorkloadRequestBatch) {
                    this.mainService.getMessagingService().sendWorkloadRequestMessage(request);
                }
            }
        }
    }

    public void checkIfWorkloadHasFinished() {
        if (this.workloadRequestsToDistribute.isEmpty()) {
            if (checkIfWorkloadInCurrentBatchAreDone()) {
                this.mainService.setState(UGEGreed.UGEGReedStat.JOINED);
                writeAndFinish();
            } else {
                ArrayList<String> pairsWithNack = this.getNackResponsesNumber();
                if (pairsWithNack.size() == 0) {
                    return;
                }
                if (this.workloadResults.size() == (this.mainService.getPairs().size() - pairsWithNack.size())) {
                    // on envoie just un NACK a la foie
                    String peerToSendId = pairsWithNack.get(0);
                    String pairToSendTo = getFirstDonePair();
                    if (pairToSendTo == null) {
                        writeAndFinish();
                        return;
                    }

                    UGEGreedWorkloadRequest messageToResend = this.workloadDistribution.get(peerToSendId);
                    UGEGreedWorkloadRequest message = this.mainService.getMessagingService().sendWorkloadRequestMessage(
                            new ServerConfig(pairToSendTo),
                            messageToResend.getWorkloadId(),
                            messageToResend.getStartValue(),
                            messageToResend.getEndValue(),
                            messageToResend.getFullyQualifiedName(),
                            messageToResend.getJarUrl()
                    );

                    this.workloadDistribution.put(pairToSendTo, message);
                    this.pairsResponses.put(pairToSendTo, UGEGreedWorkloadRequestStatus.REQUESTED);
                }
            }
        }
    }
}
