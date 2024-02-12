package org.uge.greed.messaging.messages;

import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.nio.ByteBuffer;

public class UGEGreedReconnectedOk implements UGEGreedBaseMessage {
    protected UGEGreedOperation operation;
    protected ServerConfig parent;

    public UGEGreedReconnectedOk(ServerConfig parent) {
        this.operation = UGEGreedOperation.RECONECTED;
        this.parent = parent;
    }

    @Override
    public UGEGreedOperation getOperation() {
        return this.operation;
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer
                .putInt(14)
                .putInt(parent.ipAddressInt)
                .putInt(parent.port);
        return buffer;
    }

    @Override
    public void encodeIntoBuffer(ByteBuffer buffer) {
        buffer
                .putInt(14)
                .putInt(parent.ipAddressInt)
                .putInt(parent.port);
    }

    @Override
    public int getSize() {
        return Integer.BYTES * 3;
    }
}
