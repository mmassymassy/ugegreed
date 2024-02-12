package org.uge.greed.messaging.messages;

import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.nio.ByteBuffer;

public class UGEgreedReconnectionRefused implements UGEGreedBaseMessage {
    protected UGEGreedOperation operation;
    protected ServerConfig parent ;
    protected ServerConfig joiner ;

    public UGEgreedReconnectionRefused(ServerConfig parent, ServerConfig joiner){
        this.operation = UGEGreedOperation.RECONECTION_REFUSED;
        this.parent = parent;
        this.joiner = joiner;
    }

    public ServerConfig getParent() {
        return parent;
    }

    public ServerConfig getJoiner() {
        return joiner;
    }

    @Override
    public UGEGreedOperation getOperation() {
        return operation;
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(4 * 5);
        buffer
                .putInt(14)
                .putInt(parent.ipAddressInt)
                .putInt(parent.port)
                .putInt(joiner.ipAddressInt)
                .putInt(joiner.port);
        return buffer;
    }

    @Override
    public void encodeIntoBuffer(ByteBuffer buffer) {
        buffer
                .putInt(14)
                .putInt(parent.ipAddressInt)
                .putInt(parent.port)
                .putInt(joiner.ipAddressInt)
                .putInt(joiner.port);
    }

    @Override
    public int getSize() {
        return Integer.BYTES * 5;
    }
}
