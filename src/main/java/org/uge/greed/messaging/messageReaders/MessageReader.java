package org.uge.greed.messaging.messageReaders;

import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.messaging.messages.*;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class MessageReader implements Reader<UGEGreedBaseMessage> {
    private enum State {
        DONE, WAITING, ERROR
    }

    ;
    private final IntReader intReader = new IntReader();
    private final StringReader stringReader = new StringReader();
    private final LongReader longReader = new LongReader();
    private State state = State.WAITING;
    private UGEGreedBaseMessage message;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        ProcessStatus operationStatus = intReader.process(buffer);

        if (operationStatus == ProcessStatus.DONE) {
            int status = intReader.get();
            switch (status) {
                case 0:
                    UGEGreedOperation operation = UGEGreedOperation.JOIN_REQUEST;
                    return readJoinRequest(buffer);
                case 1:
                    operation = UGEGreedOperation.JOIN_REQUEST_BROADCAST;
                    return readJoinRequestBroadcast(buffer);
                case 2:
                    operation = UGEGreedOperation.PEER_LIST;
                    return readPeerListMessage(buffer);
                case 3:
                    operation = UGEGreedOperation.WORKLOAD_REQUEST;
                    return readWorkloadRequestMessage(buffer);
                case 4:
                    operation = UGEGreedOperation.WORKLOAD_RESULT;
                    return this.readWorkloadResultMessage(buffer);
                case 5:
                    operation = UGEGreedOperation.LEAVE_REQUEST;
                    return this.readLeaveRequest(buffer);
                case 6:
                    operation = UGEGreedOperation.WORKLOAD_ACK;
                    return readWorkloadAkcMessage(buffer);
                case 7:
                    operation = UGEGreedOperation.WORKLOAD_NACK;
                    return readWorkloadNAkcMessage(buffer);
                case 9:
                    operation = UGEGreedOperation.LEAVE_GRANTED;
                    return readLeaveGrantedMessage(buffer);
                case 10:
                    operation = UGEGreedOperation.LEAVE_REFUSED;
                    return readLeaveRefusedMessage(buffer);
                case 11:
                    operation = UGEGreedOperation.PLEASE_RECONNECT;
                    return readPleaseReconnectMessage(buffer);
                case 12:
                    operation = UGEGreedOperation.RECONNECTION_REQUEST;
                    return readReconnectionRequestMessage(buffer);
                case 13:
                    operation = UGEGreedOperation.RECONECTION_ACCEPTED;
                    return readReconnectionAcceptedMessage(buffer);
                case 14:
                    operation = UGEGreedOperation.RECONECTED;
                    return readReconnectedMessage(buffer);
                case 15:
                    operation = UGEGreedOperation.RECONECTION_REFUSED;
                    return readReconnectionRefusedMessage(buffer);
                case 16:
                    operation = UGEGreedOperation.LEAVING_BROADCAST;
                    return readLeavingBroadcastMessage(buffer);
                default:
                    state = State.ERROR;
                    return ProcessStatus.ERROR;
            }
        } else if (operationStatus == ProcessStatus.ERROR) {
            this.state = State.ERROR;
        } else if (operationStatus == ProcessStatus.REFILL) {
            this.state = State.WAITING;
        }


        return operationStatus;
    }

    private int readInt(ByteBuffer buffer) {
        intReader.reset();
        ProcessStatus operationStatus = intReader.process(buffer);
        if (operationStatus != ProcessStatus.DONE) {
            return -1;
        }
        return intReader.get();
    }

    private long readLong(ByteBuffer buffer) {
        longReader.reset();
        ProcessStatus operationStatus = longReader.process(buffer);
        if (operationStatus != ProcessStatus.DONE) {
            return -1;
        }
        return longReader.get();
    }

    private ServerConfig readServerConfig(ByteBuffer buffer) {
        intReader.reset();
        int address = readInt(buffer);
        int port = readInt(buffer);

        return new ServerConfig(address, port);
    }


    private String readString(ByteBuffer buffer) {
        stringReader.reset();
        ProcessStatus operationStatus = stringReader.process(buffer);
        if (operationStatus != ProcessStatus.DONE) {
            return null;
        }
        return stringReader.get();
    }

    private ProcessStatus readPleaseReconnectMessage(ByteBuffer buffer) {
        intReader.reset();
        ServerConfig parent = readServerConfig(buffer);
        this.message = new UGEGreedPleaseReconnectMessage(parent);
        this.state = State.DONE;
        return ProcessStatus.DONE;
    }

    private ProcessStatus readReconnectedMessage(ByteBuffer buffer) {
        ServerConfig parent = readServerConfig(buffer);
        this.message = new UGEGreedReconnectedOk(parent);
        this.state = State.DONE;
        return ProcessStatus.DONE;
    }

    private ProcessStatus readReconnectionRequestMessage(ByteBuffer buffer) {
        intReader.reset();
        ServerConfig joiner = readServerConfig(buffer);
        ServerConfig destination = readServerConfig(buffer);

        this.message = new UGEGreedReconnectionRequestMessage(joiner, destination);
        this.state = State.DONE;
        return ProcessStatus.DONE;
    }

    private ProcessStatus readLeaveGrantedMessage(ByteBuffer buffer) {
        intReader.reset();
        this.message = new UGEGreedLeaveGranted();
        this.state = State.DONE;
        return ProcessStatus.DONE;
    }

    private ProcessStatus readLeavingBroadcastMessage(ByteBuffer buffer) {
        intReader.reset();

        int leaverAddress = this.readInt(buffer);
        int leaverPort = readInt(buffer);

        int senderAddress = this.readInt(buffer);
        int senderPort = readInt(buffer);

        int parentAddress = this.readInt(buffer);
        int parentPort = readInt(buffer);
        this.state = State.DONE;
        this.message = new UGEGreedLeavingBroadcast(leaverAddress, leaverPort, senderAddress, senderPort, parentAddress, parentPort);
        return ProcessStatus.DONE;
    }

    private ProcessStatus readLeaveRefusedMessage(ByteBuffer buffer) {
        intReader.reset();
        this.message = new UGEGreedLeaveRefused();
        this.state = State.DONE;
        return ProcessStatus.DONE;
    }

    private ProcessStatus readReconnectionRefusedMessage(ByteBuffer buffer) {
        intReader.reset();
        ServerConfig parent = readServerConfig(buffer);
        ServerConfig joiner = readServerConfig(buffer);
        this.message = new UGEgreedReconnectionRefused(parent, joiner);
        this.state = State.DONE;
        return ProcessStatus.DONE;
    }

    private ProcessStatus readReconnectionAcceptedMessage(ByteBuffer buffer) {
        intReader.reset();
        ServerConfig parent = readServerConfig(buffer);
        ServerConfig joiner = readServerConfig(buffer);
        this.message = new UGEgreedReconnectionAccepted(parent, joiner);
        this.state = State.DONE;
        return ProcessStatus.DONE;
    }

    private ProcessStatus readWorkloadResultMessage(ByteBuffer buffer) {
        intReader.reset();
        ServerConfig source = readServerConfig(buffer);
        ServerConfig destination = readServerConfig(buffer);
        int workloadId = readInt(buffer);
        String results = readString(buffer);
        Long calculatedValue = readLong(buffer);

        this.message = new UGEGreedWorkloadResult(source, destination, workloadId, results, calculatedValue);
        this.state = State.DONE;
        return ProcessStatus.DONE;
    }

    private ProcessStatus readWorkloadRequestMessage(ByteBuffer buffer) {
        intReader.reset();
        ServerConfig sender = readServerConfig(buffer);
        ServerConfig destination = readServerConfig(buffer);
        int workloadId = readInt(buffer);

        long startValue = readLong(buffer);
        long endValue = readLong(buffer);
        String fullyQualifiedName = readString(buffer);
        String url = readString(buffer);

        try {
            URL urlObj = new URL(url);
            this.message = new UGEGreedWorkloadRequest(
                    sender,
                    destination,
                    workloadId,
                    startValue,
                    endValue,
                    fullyQualifiedName,
                    urlObj
            );
            this.state = State.DONE;
            return ProcessStatus.DONE;
        } catch (MalformedURLException e) {
            this.state = State.ERROR;
            return ProcessStatus.ERROR;
        }


    }

    private ProcessStatus readWorkloadNAkcMessage(ByteBuffer buffer) {
        intReader.reset();
        int workloadId = readInt(buffer);
        ServerConfig source = readServerConfig(buffer);
        ServerConfig destination = readServerConfig(buffer);

        this.message = new UGEGreedWorkloadNAckMessage(workloadId, source, destination);
        this.state = State.DONE;
        return ProcessStatus.DONE;
    }

    private ProcessStatus readWorkloadAkcMessage(ByteBuffer buffer) {
        intReader.reset();
        int workloadId = readInt(buffer);
        ServerConfig source = readServerConfig(buffer);
        ServerConfig destination = readServerConfig(buffer);

        this.message = new UGEGreedWorkloadAckMessage(workloadId, source, destination);
        this.state = State.DONE;
        return ProcessStatus.DONE;
    }

    private ProcessStatus readPeerListMessage(ByteBuffer buffer) {
        intReader.reset();
        ServerConfig sender = readServerConfig(buffer);
        int numberOfPeers = readInt(buffer);
        Set<ServerConfig> peers = new HashSet<>();
        for (int i = 0; i < numberOfPeers; i++) {
            ServerConfig peer = readServerConfig(buffer);
            peers.add(peer);
        }
        this.state = State.DONE;
        this.message = new UGEGreedPeerListBaseMessage(sender, peers);
        return ProcessStatus.DONE;
    }


    private ProcessStatus readJoinRequestBroadcast(ByteBuffer buffer) {
        intReader.reset();

        int joinerAddress = this.readInt(buffer);
        int joinerPort = readInt(buffer);

        int senderAddress = this.readInt(buffer);
        int senderPort = readInt(buffer);
        this.state = State.DONE;
        this.message = new UGEGreedJoinRequestBroadcastBaseMessage(joinerAddress, joinerPort, senderAddress, senderPort);
        return ProcessStatus.DONE;
    }

    private ProcessStatus readJoinRequest(ByteBuffer buffer) {
        UGEGreedOperation operation = UGEGreedOperation.JOIN_REQUEST;
        intReader.reset();
        ProcessStatus operationStatus = intReader.process(buffer);

        if (operationStatus != ProcessStatus.DONE) {
            return operationStatus;
        }

        int address = intReader.get();
        intReader.reset();
        operationStatus = intReader.process(buffer);
        if (operationStatus != ProcessStatus.DONE) {
            return operationStatus;
        }
        int port = intReader.get();
        this.state = State.DONE;
        this.message = new UGEGreedJoinRequestBaseMessage(address, port);
        return ProcessStatus.DONE;
    }

    private ProcessStatus readLeaveRequest(ByteBuffer buffer) {
        UGEGreedOperation operation = UGEGreedOperation.LEAVE_REQUEST;
        intReader.reset();
        ProcessStatus operationStatus = intReader.process(buffer);

        if (operationStatus != ProcessStatus.DONE) {
            return operationStatus;
        }

        int address = intReader.get();
        intReader.reset();
        operationStatus = intReader.process(buffer);
        if (operationStatus != ProcessStatus.DONE) {
            return operationStatus;
        }
        int port = intReader.get();

        int numberOfChildren = readInt(buffer);
        Set<ServerConfig> children = new HashSet<>();
        for (int i = 0; i < numberOfChildren; i++) {
            ServerConfig peer = readServerConfig(buffer);
            children.add(peer);
        }

        this.state = State.DONE;
        this.message = new UGEGreedLeaveRequestMessage(address, port, children);
        return ProcessStatus.DONE;
    }

    @Override
    public UGEGreedBaseMessage get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return this.message;
    }

    @Override
    public void reset() {
        state = State.WAITING;
        intReader.reset();
        stringReader.reset();
        message = null;
    }
}
