package org.uge.greed.greed;

import org.uge.greed.commands.CommandValidator;
import org.uge.greed.commands.StartCommand;
import org.uge.greed.deconnection.UGEGreedDeconnectionService;
import org.uge.greed.execution.UGEGreddExcutionService;
import org.uge.greed.messaging.messages.UGEGreedWorkloadRequest;
import org.uge.greed.network.routing.UGEGreedRouterService;
import org.uge.greed.network.tcp.client.Client;
import org.uge.greed.messaging.messages.UGEGreedBaseMessage;
import org.uge.greed.messaging.UGEGreedMessagingService;
import org.uge.greed.messaging.messages.UGEGreedJoinRequestBaseMessage;
import org.uge.greed.network.tcp.server.Server;
import org.uge.greed.network.tcp.server.ServerConfig;
import org.uge.greed.workloadRequest.UGEGreedWorkloadRequestService;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;


public class UGEGreed {

    private static Logger logger = Logger.getLogger(Client.class.getName());
    public enum UGEGReedStat {
        STARTING,
        REQUESTED_JOIN,
        JOINED,
        HAS_SENT_WORKLOAD,
        DISCONNECTING,
    }

    private UGEGReedStat serviceState = UGEGReedStat.STARTING;
    protected ServerConfig serverConfig;
    private Server server;
    private Client client;
    private Boolean clientWasInitiated = false ;
    private final UGEGreedMessagingService messagingService = new UGEGreedMessagingService(this);
    private final UGEGreedRouterService routerService = new UGEGreedRouterService(this);
    private final UGEGreddExcutionService excutionService = new UGEGreddExcutionService(this);
    private final UGEGreedWorkloadRequestService workloadRequestService = new UGEGreedWorkloadRequestService(this);
    private final UGEGreedDeconnectionService deconnectionService = new UGEGreedDeconnectionService(this);
    private ServerConfig parent = null;
    private Set<ServerConfig> pairs;

    private ServerConfig nextParent = null ;

    public void setNextParent(ServerConfig nextParent) {
        this.nextParent = nextParent;
    }


    private Set<ServerConfig> children = new HashSet<>();
    private final ArrayBlockingQueue<StartCommand> commandsToStart = new ArrayBlockingQueue<>(10);
    public Client getClient() {
        return client;
    }
    public Boolean getClientWasInitiated() {
        return clientWasInitiated;
    }

    public void setClientWasInitiated(Boolean clientWasInitiated) {
        this.clientWasInitiated = clientWasInitiated;
    }

    public UGEGreed(String host, int port) throws IOException {
        this.serverConfig = new ServerConfig(host, port);
        this.server = new Server(this);
        this.pairs = new HashSet<>();
        this.serviceState = UGEGReedStat.JOINED;
    }


    public UGEGreed(String host, int port, String parentHost, int parentPort) throws IOException {
        this(host, port);
        this.parent = new ServerConfig(parentHost, parentPort);
        this.initialiseClient();
    }

    public void setState(UGEGReedStat state) {
        this.serviceState = state;
    }

    public UGEGReedStat getState() {
        return this.serviceState;
    }

    public void updateParent(ServerConfig newParent){
        this.parent = newParent ;
        this.client.disconnect();
        this.initialiseClient();
        this.serviceState = UGEGReedStat.JOINED ;
    }
    public void initialiseClient() {
        if(!this.clientWasInitiated){
            this.setState(UGEGReedStat.STARTING);
        }
        if (this.parent != null) {
            this.client = this.client == null ? new Client(this) : this.client;
            this.client.connect();
        }
    }

    public Set<ServerConfig> getChildren() {
        return children;
    }

    public void addToChildren(String ip, int port) {
        this.children.add(new ServerConfig(ip, port));
    }

    public boolean isChild(String ip, int port) {
        return this.children.contains(new ServerConfig(ip, port));
    }

    public UGEGreedRouterService getRouterService() {
        return this.routerService;
    }

    public UGEGreedDeconnectionService getDeconnectionService() {
        return this.deconnectionService;
    }
    public ServerConfig getParent() {
        return parent;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public Server getServer() {
        return server;
    }

    public UGEGreedWorkloadRequestService getWorkloadRequestService() {
        return this.workloadRequestService;
    }

    private void consoleRun() {
        try {
            Scanner reader = new java.util.Scanner(System.in);
            while (reader.hasNextLine()) {
                String command = reader.nextLine();
                String[] words = command.split(" ");
                if (words.length == 0) {
                    continue;
                }
                switch (words[0]) {
                    case "START":

                        StartCommand startCommand = CommandValidator.validateStartCommand(command);
                        sendStartCommand(startCommand);
                        break;
                    case "DISCONNECT":
                        this.deconnectionService.requestDeconnection();
                        break;
                    default:
                        System.out.println("Commande introuvale utiliser HELP pour plus d'informations.");
                }
            }
        } catch (Exception e) {
            // pass
        }
    }

    public UGEGreddExcutionService getExcutionService() {
        return this.excutionService;
    }

    private void sendStartCommand(StartCommand startCommand) throws InterruptedException {
        synchronized (this.commandsToStart) {
            this.commandsToStart.put(startCommand);
        }
    }

    private void proccessComands() {
        if (this.serviceState != UGEGReedStat.JOINED) {
            return;
        }

        if(this.commandsToStart.isEmpty()){
            return;
        }

        var command = this.commandsToStart.poll();
        if (command != null) {
            System.out.println("-- STARTING COMMAND --");
            this.workloadRequestService.distributeWorkload(command);
            System.out.println("-- STARTING COMMAND DONE --");
        }

    }

    public UGEGreedMessagingService getMessagingService() {
        return this.messagingService;
    }

    public void launch() {
        Thread consoleThread = new Thread(this::consoleRun);
        consoleThread.setDaemon(true);
        consoleThread.start();

        while (!Thread.interrupted()) {
            this.server.loop();
            if (this.client != null) {
                this.client.clientLoop();
            }
            this.proccessComands();
            this.checkAndExecute();
            this.excutionService.sendIfWorkloadIsDone();
            this.workloadRequestService.sendWorkloadRequestBatch();
            if(this.nextParent != null){
                updateParent(nextParent);
                this.nextParent = null;
            }
        }
    }

    public void disconnectFromParent() {
        this.client.disconnect();
    }

    public void setParent(ServerConfig parent) {
        this.parent = parent;
    }

    public void changeParent(ServerConfig newParent) {
        this.parent = newParent;
        this.client.disconnect();
        this.initialiseClient();
    }


    public void sendMessageToParent(UGEGreedBaseMessage message) {
        this.client.sendMessage(message);
    }

    public void sendMessageToClient(String clientId, UGEGreedBaseMessage message) {
        this.server.sendToClient(clientId, message);
    }

    public void sendToAllClients(UGEGreedBaseMessage message) {
        this.server.sendToAllClients(message);
    }

    public void sendToAllClientsBut(String eliminate, UGEGreedBaseMessage message) {
        this.server.sendToAllClientsBut(eliminate, message);
    }

    public void sendJoinRequest() {
        System.out.println("info: sending join request");
        UGEGreedBaseMessage message = new UGEGreedJoinRequestBaseMessage(this.serverConfig.ipAddress, this.serverConfig.port);
        this.setState(UGEGReedStat.REQUESTED_JOIN);
        this.sendMessageToParent(message);
    }

    public void handleMessageIn(UGEGreedBaseMessage message, SelectionKey key) {
        this.messagingService.processMessage(message, key);
    }

    public void checkAndExecute() {
        this.excutionService.checkAndExecute();
    }

    public void addPair(String ip, int port) {
        this.pairs.add(new ServerConfig(ip, port));
    }

    public boolean handleTaskRequest(UGEGreedWorkloadRequest message) {
        if (this.excutionService.isBusy()) {
            return false;
        }

        this.excutionService.setCommande(
                new StartCommand(
                        message.getJarUrl(),
                        message.getFullyQualifiedName(),
                        message.getStartValue(),
                        message.getEndValue(),
                        "HOLDER"
                ),
                message.getSender(),
                message.getWorkloadId(),
                message
        );

        return true;
    }

    public void removePair(String ip, int port) {
        this.pairs.remove(new ServerConfig(ip, port));
    }

    public Set<ServerConfig> getPairs() {
        return this.pairs;
    }

    public void removeChildren(String ip, int port) {
        this.children.remove(new ServerConfig(ip, port));
    }

    public void shutdown() {
        System.out.println("SHUTTING DOWN");
        this.client.disconnect();
        System.exit(0);
    }

}
