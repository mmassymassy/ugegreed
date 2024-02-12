package org.uge.greed.messaging.messages;

import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.nio.ByteBuffer;

public class UGEGreedReconnectionRequestMessage implements UGEGreedBaseMessage {
    protected UGEGreedOperation operation;
    protected ServerConfig joiner ;
    protected ServerConfig destination ;

    public UGEGreedReconnectionRequestMessage(ServerConfig joiner, ServerConfig destination ){
        this.operation = UGEGreedOperation.RECONNECTION_REQUEST;
        this.joiner = joiner;
        this.destination = destination;
    }

    @Override
    public UGEGreedOperation getOperation() {
        return this.operation;
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer
                .putInt(12)
                .putInt(joiner.ipAddressInt)
                .putInt(joiner.port)
                .putInt(destination.ipAddressInt)
                .putInt(destination.port);
        return buffer;
    }

    @Override
    public void encodeIntoBuffer(ByteBuffer buffer) {
        buffer
                .putInt(12)
                .putInt(joiner.ipAddressInt)
                .putInt(joiner.port)
                .putInt(destination.ipAddressInt)
                .putInt(destination.port);
    }

    @Override
    public int getSize() {
        return Integer.BYTES * 5;
    }

    public ServerConfig getJoiner() {
        return joiner;
    }

    public ServerConfig getDestination() {
        return destination;
    }

    @Override
    public String toString() {
        return "UGEGreedReconnectionRequestMessage{" +
                "operation=" + operation +
                ", joiner=" + joiner +
                ", destination=" + destination +
                '}';
    }
}
