package org.uge.greed.messaging.messages;

import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.nio.ByteBuffer;

public class UGEGreedWorkloadAckMessage implements UGEGreedBaseMessage{
    private int workloadId ;
    private ServerConfig source;
    private ServerConfig destination;
    private UGEGreedOperation operation ;

    public UGEGreedWorkloadAckMessage(int workloadId, ServerConfig source, ServerConfig destination){
        this.operation = UGEGreedOperation.WORKLOAD_ACK;
        this.workloadId = workloadId;
        this.source = source;
        this.destination = destination;
    }

    @Override
    public UGEGreedOperation getOperation() {
        return operation;
    }

    public ByteBuffer encode(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer
                .putInt(6)
                .putInt(workloadId)
                .putInt(source.ipAddressInt)
                .putInt(source.port)
                .putInt(destination.ipAddressInt)
                .putInt(destination.port);
        return buffer;
    }

    public void encodeIntoBuffer(ByteBuffer buffer){
        buffer
                .putInt(6)
                .putInt(workloadId)
                .putInt(source.ipAddressInt)
                .putInt(source.port)
                .putInt(destination.ipAddressInt)
                .putInt(destination.port);
    }

    @Override
    public int getSize() {
        return Integer.BYTES * 6;
    }

    public int getWorkloadId() {
        return workloadId;
    }

    public ServerConfig getSource() {
        return source;
    }

    public ServerConfig getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "UGEGreedWorkloadAckMessage{" +
                "workloadId=" + workloadId +
                ", source=" + source +
                ", destination=" + destination +
                ", operation=" + operation +
                '}';
    }
}
