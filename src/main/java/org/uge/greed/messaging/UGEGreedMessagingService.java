package org.uge.greed.messaging;

import org.uge.greed.deconnection.UGEGreedDeconnectionService;
import org.uge.greed.greed.UGEGreed;
import org.uge.greed.messaging.messages.*;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.net.URL;
import java.nio.channels.SelectionKey;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class UGEGreedMessagingService {
    private UGEGreed mainService;

    public UGEGreedMessagingService(UGEGreed mainService) {
        this.mainService = mainService;
    }

    public void processMessage(UGEGreedBaseMessage message, SelectionKey key) {
        System.out.println("-- MESSAGE IN --");
        System.out.println("info: type de message: " + message.getOperation());
        switch (message.getOperation()) {
            case JOIN_REQUEST:
                processJoinRequestMessage(message, key);
                break;
            case JOIN_REQUEST_BROADCAST:
                processJoinRequestBroadcastMessage(message);
                break;
            case PEER_LIST:
                processPeerListMessage(message);
                break;
            case LEAVE_REQUEST:
                processLeaveRequest(message);
                break;
            case WORKLOAD_RESULT:
                processWorkloadResultMessage(message);
                break;
            case WORKLOAD_REQUEST:
                processWorkloadRequestMessage(message);
                break;
            case WORKLOAD_ACK:
                processWorkloadAckMessage(message);
                break;
            case WORKLOAD_NACK:
                processWorkloadNAckMessage(message);
                break;
            case LEAVE_GRANTED:
                processLeaveGrantedMessage(message);
                break;
            case LEAVE_REFUSED:
                processLeaveRefusedMessage(message);
                break;
            case PLEASE_RECONNECT:
                processPleaseReconnectMessage(message);
                break;
            case RECONNECTION_REQUEST:
                processReconnectionRequestMessage(message);
                break;
            case RECONECTION_REFUSED:
                processReconnectionRefusedMessage(message);
                break;
            case RECONECTION_ACCEPTED:
                processReconnectionAcceptedMessage(message);
                break;
            case RECONECTED:
                processReconnectedMessage(message);
                break;
            case LEAVING_BROADCAST:
                processLeavingBroadcastMessage(message);
                break;
            default:
                System.out.println("info: Message non traité");
                break;
        }
        System.out.println("-- MESSAGE IN DONE --");
    }

    public UGEGreedWorkloadRequest createWorkloadRequestMessage(ServerConfig destination, int workloadId, long startValue, long endValue, String fullyQualifiedName, URL jarUrl) {
        UGEGreedWorkloadRequest workloadRequestMessage = new UGEGreedWorkloadRequest(
                this.mainService.getServerConfig(),
                destination,
                workloadId,
                startValue,
                endValue,
                fullyQualifiedName,
                jarUrl
        );
        return workloadRequestMessage;
    }

    public void sendWorkloadRequestMessage(UGEGreedWorkloadRequest message) {
        System.out.println("info: Envoie du message WORKLOAD_REQUEST vers: " + message.getDestination().getAppId());
        this.mainService.getRouterService().routeMessage(message.getDestination().getAppId(), message);
    }

    public void processLeavingBroadcastMessage(UGEGreedBaseMessage message) {
        UGEGreedLeavingBroadcast leavingBroadcastMessage = (UGEGreedLeavingBroadcast) message;
        System.out.println("info: Processing du message LEAVE_BROADCAST");


        // mise a jour des entrer passant par le noeud qui part
        this.mainService.getRouterService().updateAllForAppId(
                leavingBroadcastMessage.getLeaverAddress() + ":" + leavingBroadcastMessage.getLeaverPort(),
                leavingBroadcastMessage.getParentAddress() + ":" + leavingBroadcastMessage.getParentPort()
        );

        // supperssion de la route vers le noeud qui part
        this.mainService.getRouterService().removeRoute(leavingBroadcastMessage.getLeaverAddress() + ":" + leavingBroadcastMessage.getLeaverPort());


        this.mainService.removePair(leavingBroadcastMessage.getLeaverAddress() ,leavingBroadcastMessage.getLeaverPort());
        this.mainService.removeChildren(leavingBroadcastMessage.getLeaverAddress() ,leavingBroadcastMessage.getLeaverPort());
        if(new ServerConfig(leavingBroadcastMessage.getLeaverAddress(),leavingBroadcastMessage.getLeaverPort()).equals(this.mainService.getParent())){
            this.mainService.setNextParent(
                    new ServerConfig(
                            leavingBroadcastMessage.getParentAddress(),
                            leavingBroadcastMessage.getParentPort()
                    )
            );
        }else{
            this.mainService.getServer().disconnectClient(leavingBroadcastMessage.getLeaverAddress() + ":" + leavingBroadcastMessage.getLeaverPort());
        }

        UGEGreedLeavingBroadcast broadcastMessage = new UGEGreedLeavingBroadcast(
                leavingBroadcastMessage.getLeaverAddress(),
                leavingBroadcastMessage.getLeaverPort(),
                this.mainService.getServerConfig().ipAddress,
                this.mainService.getServerConfig().port,
                leavingBroadcastMessage.getParentAddress(),
                leavingBroadcastMessage.getParentPort()
        );
        System.out.println("info: Envoie du message" + broadcastMessage.getOperation());
        this.mainService.getRouterService().broadcastMessageToAllBut(leavingBroadcastMessage.getSenderAddress() + ":" + leavingBroadcastMessage.getSenderPort(), broadcastMessage);
    }

    public UGEGreedWorkloadRequest sendWorkloadRequestMessage(ServerConfig destination, int workloadId, long startValue, long endValue, String fullyQualifiedName, URL jarUrl) {
        UGEGreedWorkloadRequest workloadRequestMessage = new UGEGreedWorkloadRequest(
                this.mainService.getServerConfig(),
                destination,
                workloadId,
                startValue,
                endValue,
                fullyQualifiedName,
                jarUrl
        );
        this.mainService.getRouterService().routeMessage(destination.getAppId(), workloadRequestMessage);
        return workloadRequestMessage;
    }

    private void processLeaveRefusedMessage(UGEGreedBaseMessage message) {
        System.out.println("info: le parent a refuse la demande de deconnection");
    }

    private void processLeaveGrantedMessage(UGEGreedBaseMessage message) {
        System.out.println("info: Processing du message LEAVE_GRANTED");
        System.out.println("info: le parent a accepte la demande de deconnection");
        UGEGreedPleaseReconnectMessage reconnectMessage = new UGEGreedPleaseReconnectMessage(this.mainService.getParent());
        int numberOfClients = this.mainService.getServer().sendToAllClients(reconnectMessage);
        System.out.println("info: " + numberOfClients + " clients ont ete onvoyer un message de please_reconnect");
        this.mainService.getDeconnectionService().setNumberOfAwaitedReconnectedFromChildren(numberOfClients);
        if (numberOfClients > 0) {
            this.mainService.getDeconnectionService().setIsChildDisconecting(true);
            this.mainService.getDeconnectionService().setState(UGEGreedDeconnectionService.DeconnectionState.ACCEPTED_WAITING_FOR_CHILDREN);
        } else {
            this.mainService.shutdown();
        }
    }

    private void processPleaseReconnectMessage(UGEGreedBaseMessage message) {
        UGEGreedPleaseReconnectMessage pleaseReconnectMessage = (UGEGreedPleaseReconnectMessage) message;
        System.out.println("info: le parent a demande de se reconnecter a " + pleaseReconnectMessage.getParent().getAppId());
        sendReconnectionRequestMessage(pleaseReconnectMessage.getParent());
    }

    private void processReconnectionRefusedMessage(UGEGreedBaseMessage message) {
        UGEgreedReconnectionRefused reconnectionRefusedMessage = (UGEgreedReconnectionRefused) message;
        System.out.println("info: le parent a refuse la demande de reconnection ");
        this.mainService.getDeconnectionService().requestDeconnection();
    }

    private void processReconnectionRequestMessage(UGEGreedBaseMessage message) {
        System.out.println("info: Processing du message RECONNECTION_REQUEST");
        UGEGreedReconnectionRequestMessage reconnectionRequestMessage = (UGEGreedReconnectionRequestMessage) message;
        if (reconnectionRequestMessage.getDestination().equals(this.mainService.getServerConfig())) {
            if (this.mainService.getDeconnectionService().getState() == UGEGreedDeconnectionService.DeconnectionState.IDLE) {
                System.out.println("info: on a accepte la demande de reconnection de " + reconnectionRequestMessage.getJoiner().getAppId());

                UGEgreedReconnectionAccepted reconnectionGrantedMessage = new UGEgreedReconnectionAccepted(
                        this.mainService.getServerConfig(),
                        reconnectionRequestMessage.getJoiner()
                );

                this.mainService.getRouterService().routeMessage(
                        reconnectionRequestMessage.getJoiner().getAppId(),
                        reconnectionGrantedMessage
                );

            } else {
                System.out.println("info: refus de demande de reconnection de " + reconnectionRequestMessage.getJoiner().getAppId());

                System.out.println("info: on a pas du parent envoie reconnection_refused");
                UGEgreedReconnectionRefused reconnectionRefusedMessage = new UGEgreedReconnectionRefused(
                        this.mainService.getServerConfig(),
                        reconnectionRequestMessage.getJoiner()
                );

            }
        } else {
            this.mainService.getRouterService().routeMessage(
                    reconnectionRequestMessage.getDestination().getAppId(),
                    reconnectionRequestMessage
            );
        }
    }

    private void sendReconnectionRequestMessage(ServerConfig parent) {
        UGEGreedReconnectionRequestMessage reconnectionRequestMessage = new UGEGreedReconnectionRequestMessage(
                this.mainService.getServerConfig(), parent
        );
        this.mainService.getRouterService().routeMessage(parent.getAppId(), reconnectionRequestMessage);
    }

    private void processWorkloadRequestMessage(UGEGreedBaseMessage message) {
        UGEGreedWorkloadRequest workloadRequestMessage = (UGEGreedWorkloadRequest) message;

        if (workloadRequestMessage.getDestination().equals(this.mainService.getServerConfig())) {
            System.out.println("info: Processing du message " + workloadRequestMessage.getOperation());
            if (!this.mainService.getExcutionService().isBusy()) {
                sendWorkloadAckMessage(workloadRequestMessage.getSender(), workloadRequestMessage.getWorkloadId());
                this.mainService.handleTaskRequest(workloadRequestMessage);
            } else {
                sendWorkloadNAckMessage(workloadRequestMessage.getSender(), workloadRequestMessage.getWorkloadId());
            }
        } else {
            this.mainService.getRouterService().routeMessage(workloadRequestMessage.getDestination().getAppId(), workloadRequestMessage);
        }
    }

    public void processReconnectionAcceptedMessage(UGEGreedBaseMessage message) {
        System.out.println("info: Processing du message RECONNECTION_ACCEPTED");
        UGEgreedReconnectionAccepted reconnectionAcceptedMessage = (UGEgreedReconnectionAccepted) message;
        if (reconnectionAcceptedMessage.getJoiner().equals(this.mainService.getServerConfig())) {
            System.out.println("info: le parent a accepte la demande de reconnection ");
            this.mainService.getDeconnectionService().setState(UGEGreedDeconnectionService.DeconnectionState.IDLE);
            UGEGreedReconnectedOk reconnectedOkMessage = new UGEGreedReconnectedOk(
                    reconnectionAcceptedMessage.getParent()
            );

            this.mainService.getRouterService().routeMessage(
                    this.mainService.getParent().getAppId(),
                    reconnectedOkMessage
            );
        }else{
            System.out.println(reconnectionAcceptedMessage);
            this.mainService.getRouterService().routeMessage(
                    reconnectionAcceptedMessage.getJoiner().getAppId(),
                    reconnectionAcceptedMessage
            );


        }

    }

    public void processWorkloadAckMessage(UGEGreedBaseMessage message) {
        UGEGreedWorkloadAckMessage workloadAckMessage = (UGEGreedWorkloadAckMessage) message;
        if (workloadAckMessage.getDestination().equals(this.mainService.getServerConfig())) {
            System.out.println("info: Processing du message " + workloadAckMessage.getOperation());
            this.mainService.getWorkloadRequestService().setWorkloadRequestResponseForPeerASAck(workloadAckMessage.getSource().getAppId());
        } else {
            this.mainService.getRouterService().routeMessage(workloadAckMessage.getDestination().getAppId(), workloadAckMessage);
        }
    }

    private void processWorkloadNAckMessage(UGEGreedBaseMessage message) {
        UGEGreedWorkloadNAckMessage workloadAckMessage = (UGEGreedWorkloadNAckMessage) message;
        if (workloadAckMessage.getDestination().equals(this.mainService.getServerConfig())) {
            System.out.println("info: Processing du message " + workloadAckMessage.getOperation());
            this.mainService.getWorkloadRequestService().setWorkloadRequestResponseForPeerASNAck(workloadAckMessage.getSource().getAppId());
        } else {
            this.mainService.getRouterService().routeMessage(workloadAckMessage.getDestination().getAppId(), workloadAckMessage);
        }
    }

    private void sendWorkloadAckMessage(ServerConfig destination, int workloadId) {
        UGEGreedWorkloadAckMessage workloadAckMessage = new UGEGreedWorkloadAckMessage(workloadId, this.mainService.getServerConfig(), destination);
        System.out.println("Info: Envoie du message " + workloadAckMessage.getOperation());
        this.mainService.getRouterService().routeMessage(destination.getAppId(), workloadAckMessage);
    }

    public void sendWorkloadNAckMessage(ServerConfig destination, int workloadId) {
        UGEGreedWorkloadNAckMessage workloadAckMessage = new UGEGreedWorkloadNAckMessage(workloadId, this.mainService.getServerConfig(), destination);
        System.out.println("Info: Envoie du message " + workloadAckMessage.getOperation());
        this.mainService.getRouterService().routeMessage(destination.getAppId(), workloadAckMessage);
    }

    public UGEGreedWorkloadResult createWorkloadRequestMessage(ServerConfig destination, int workloadId, String workloadResult, Long calculatedValue) {
        return new UGEGreedWorkloadResult(this.mainService.getServerConfig(), destination, workloadId, workloadResult, calculatedValue);
    }

    private void processReconnectedMessage(UGEGreedBaseMessage message) {
        UGEGreedReconnectedOk reconnectedOkMessage = (UGEGreedReconnectedOk) message;
        System.out.println("info: Processing du message RECONNECTED OK");
        System.out.println("info: le parent a accepte la demande de reconnection d'un fils");
        if (this.mainService.getDeconnectionService().descreaseNumberOfAwaitedReconnectedFromChildren()) {
            System.out.println("info: tous les fils ont reussi a se reconnecter");
            UGEGreedLeavingBroadcast leaveMessage = new UGEGreedLeavingBroadcast(
                    this.mainService.getServerConfig().ipAddress,
                    this.mainService.getServerConfig().port,
                    this.mainService.getServerConfig().ipAddress,
                    this.mainService.getServerConfig().port,
                    this.mainService.getParent().ipAddress,
                    this.mainService.getParent().port
            );
            this.mainService.getRouterService().broadcastMessageToAllBut(
                    this.mainService.getServerConfig().ipAddress,
                    leaveMessage
            );

            System.out.println("info: Le programme va quitter dans 10 secondes");
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("info: Le programme va quitter maintenant");
                    System.exit(0);
                    // code to execute after delay
                }
            }, 10 * 1000);
        } else {
            System.out.println("info: attend de recevoir tous les messages de reconnection des fils. Il reste " + this.mainService.getDeconnectionService().getNumberOfAwaitedReconnectedFromChildren() + " fils");
        }
    }

    public void sendWorkloadResultMessage(UGEGreedWorkloadResult workloadResultMessage) {
        System.out.println("Info: Envoie du message " + workloadResultMessage.getOperation());
        this.mainService.getRouterService().routeMessage(workloadResultMessage.getDestination().getAppId(), workloadResultMessage);
    }

    public void sendWorkloadResultMessage(ServerConfig destination, int workloadId, String workloadResult, Long calculatedValue) {
        UGEGreedWorkloadResult workloadResultMessage = new UGEGreedWorkloadResult(this.mainService.getServerConfig(), destination, workloadId, workloadResult, calculatedValue);
        System.out.println("Info: Envoie du message " + workloadResultMessage.getOperation());
        this.mainService.getRouterService().routeMessage(destination.getAppId(), workloadResultMessage);
    }

    private void processWorkloadResultMessage(UGEGreedBaseMessage message) {
        UGEGreedWorkloadResult workloadResultMessage = (UGEGreedWorkloadResult) message;
        if (workloadResultMessage.getDestination().equals(this.mainService.getServerConfig())) {
            System.out.println("info: Processing du message " + workloadResultMessage.getOperation());
            this.mainService.getWorkloadRequestService().addWorkloadResult(workloadResultMessage.getCalculatedValue(), workloadResultMessage.getWorkloadResult());
            if (this.mainService.getWorkloadRequestService().checkIfWorkloadRequestIsFullfilled(workloadResultMessage.getWorkloadId())) {
                this.mainService.getWorkloadRequestService().setWorkloadRequestResponseForPeerASDone(workloadResultMessage.getSource().getAppId());
            }
            this.mainService.getWorkloadRequestService().checkIfWorkloadHasFinished();

        } else {
            this.mainService.getRouterService().routeMessage(workloadResultMessage.getDestination().getAppId(), workloadResultMessage);
        }
    }

    private void processLeaveRequest(UGEGreedBaseMessage message) {
        System.out.println("info: Processing du message LEAVE REQUEST");
        UGEGreedLeaveRequestMessage leaveRequestMessage = (UGEGreedLeaveRequestMessage) message;

        if (this.mainService.getDeconnectionService().getIsDisconnectionPossible()) {
            UGEGreedLeaveGranted granted = new UGEGreedLeaveGranted();
            this.mainService.getServer().sendToClient(leaveRequestMessage.address + ":" + leaveRequestMessage.port, granted);
            this.mainService.removePair(leaveRequestMessage.address, leaveRequestMessage.port);
            this.mainService.removeChildren(leaveRequestMessage.address, leaveRequestMessage.port);
            this.mainService.getRouterService().removeRoute(leaveRequestMessage.address + ":" + leaveRequestMessage.port);
        } else {
            UGEGreedLeaveRefused refused = new UGEGreedLeaveRefused();
            this.mainService.getServer().sendToClient(leaveRequestMessage.address + ":" + leaveRequestMessage.port, refused);
        }

    }

    private void processJoinRequestBroadcastMessage(UGEGreedBaseMessage message) {
        UGEGreedJoinRequestBroadcastBaseMessage joinMessage = (UGEGreedJoinRequestBroadcastBaseMessage) message;
        System.out.println("info: Processing du message " + joinMessage.getOperation());
        this.mainService.addPair(joinMessage.joinerAddress, joinMessage.joinderPort);
        if (this.mainService.isChild(joinMessage.senderAddress, joinMessage.senderPort)) {
            this.mainService.addToChildren(joinMessage.joinerAddress, joinMessage.joinderPort);
        }

        Boolean isParent = false;
        if (this.mainService.getParent() != null) {
            isParent = this.mainService.getParent().getAppId().equals(joinMessage.senderAddress + ":" + joinMessage.senderPort);
        }
        this.mainService.getRouterService().addRoute(joinMessage.joinerAddress + ":" + joinMessage.joinderPort, isParent, !isParent, joinMessage.senderAddress + ":" + joinMessage.senderPort);

        UGEGreedJoinRequestBroadcastBaseMessage broadcastMessage = new UGEGreedJoinRequestBroadcastBaseMessage(joinMessage.joinerAddress, joinMessage.joinderPort, this.mainService.getServerConfig().ipAddress, this.mainService.getServerConfig().port);
        System.out.println("info: Envoie du message " + broadcastMessage.getOperation());
        this.mainService.getRouterService().broadcastMessageToAllBut(joinMessage.senderAddress + ":" + joinMessage.senderPort, broadcastMessage);
    }

    private void processPeerListMessage(UGEGreedBaseMessage message) {
        UGEGreedPeerListBaseMessage peerListMessage = (UGEGreedPeerListBaseMessage) message;
        System.out.println("info: Processing du message " + peerListMessage.getOperation());
        Set<ServerConfig> peers = peerListMessage.getPeers();
        ServerConfig sender = peerListMessage.getSender();

        this.mainService.addPair(sender.ipAddress, sender.port);

        this.mainService.getRouterService().addRoute(
                sender.getAppId(),
                true,
                false,
                peerListMessage.getSender().getAppId()
        );

        for (ServerConfig peer : peers) {
            this.mainService.addPair(peer.ipAddress, peer.port);
            this.mainService.getRouterService().addRoute(
                    peer.getAppId(),
                    true,
                    false,
                    peerListMessage.getSender().getAppId()
            );
        }

        this.mainService.setState(UGEGreed.UGEGReedStat.JOINED);
    }

    private void processJoinRequestMessage(UGEGreedBaseMessage message, SelectionKey key) {
        // get the join request message
        UGEGreedJoinRequestBaseMessage joinMessage = (UGEGreedJoinRequestBaseMessage) message;
        System.out.println("info: Processing du message JOING_REQUEST");
        // TODO check if this node is not disconnecting


        // add the socket channel to the server socket lists
        this.mainService.getServer().addNewClientChannelFromJoinRequest(joinMessage.address + ":" + joinMessage.port, key);

        // add the new route        this.mainService.sendMessageToClient(joinMessage.address+":"+joinMessage.port, peerListMessage);
        if(this.mainService.getRouterService().routeExists(joinMessage.address + ":" + joinMessage.port)){
            return;
        }

        this.mainService.getRouterService().addRoute(joinMessage.address + ":" + joinMessage.port, false, true, joinMessage.address + ":" + joinMessage.port);

        // create a peer list message and send it to the client
        UGEGreedPeerListBaseMessage peerListMessage = new UGEGreedPeerListBaseMessage(this.mainService.getServerConfig(), this.mainService.getPairs());
        System.out.println("info: envoie du message " + peerListMessage.getOperation());

        this.mainService.sendMessageToClient(joinMessage.address + ":" + joinMessage.port, peerListMessage);

        // add this peer ( this step is below the send peers step to avoid sending the peer list to itself)
        this.mainService.addPair(joinMessage.address, joinMessage.port);
        this.mainService.addToChildren(joinMessage.address, joinMessage.port);

        // broadcast the join request to all the peers
        UGEGreedJoinRequestBroadcastBaseMessage broadcastMessage = new UGEGreedJoinRequestBroadcastBaseMessage(
                joinMessage.address,
                joinMessage.port,
                this.mainService.getServerConfig().ipAddress,
                this.mainService.getServerConfig().port
        );
        System.out.println("info: envoie du message " + broadcastMessage.getOperation());
        this.mainService.getRouterService().broadcastMessageToAllBut(joinMessage.address + ":" + joinMessage.port, broadcastMessage);
    }

    public void sendLeaveRequestToParent() {
        UGEGreedLeaveRequestMessage leaveRequestMessage = new UGEGreedLeaveRequestMessage(
                this.mainService.getServerConfig().ipAddress,
                this.mainService.getServerConfig().port,
                this.mainService.getChildren());
        System.out.println("info: envoie du message " + leaveRequestMessage.getOperation());
        this.mainService.getClient().sendMessage(leaveRequestMessage);
    }
}
