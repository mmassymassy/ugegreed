package org.uge.greed.messaging.messages;

import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.nio.ByteBuffer;

public class UGEGreedPleaseReconnectMessage implements UGEGreedBaseMessage{
    protected UGEGreedOperation operation;
    protected ServerConfig parent ;

    public UGEGreedPleaseReconnectMessage(ServerConfig parent){
        this.operation = UGEGreedOperation.PLEASE_RECONNECT;
        this.parent = parent;
    }


    @Override
    public UGEGreedOperation getOperation() {
        return operation;
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer
                .putInt(11)
                .putInt(parent.ipAddressInt)
                .putInt(parent.port);

        return buffer;
    }

    @Override
    public void encodeIntoBuffer(ByteBuffer buffer) {
        buffer
                .putInt(11)
                .putInt(parent.ipAddressInt)
                .putInt(parent.port);
    }

    @Override
    public int getSize() {
        return Integer.BYTES * 3;
    }

    public ServerConfig getParent() {
        return parent;
    }
}
