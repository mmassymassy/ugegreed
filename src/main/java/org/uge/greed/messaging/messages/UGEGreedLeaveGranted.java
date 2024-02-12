package org.uge.greed.messaging.messages;

import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.nio.ByteBuffer;

public class UGEGreedLeaveGranted implements  UGEGreedBaseMessage {
    private UGEGreedOperation operation ;
    public UGEGreedLeaveGranted(){
        this.operation = UGEGreedOperation.LEAVE_GRANTED;
    }


    @Override
    public UGEGreedOperation getOperation() {
        return this.operation;
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(9);

        return buffer;
    }

    @Override
    public void encodeIntoBuffer(ByteBuffer buffer) {
        buffer.putInt(9);
    }

    @Override
    public int getSize() {
        return Integer.BYTES;
    }
}
