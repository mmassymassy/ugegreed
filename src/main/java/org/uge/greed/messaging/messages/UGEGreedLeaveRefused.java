package org.uge.greed.messaging.messages;

import org.uge.greed.messaging.UGEGreedOperation;

import java.nio.ByteBuffer;

public class UGEGreedLeaveRefused implements UGEGreedBaseMessage{
    protected  UGEGreedOperation operation;
    public UGEGreedLeaveRefused(){
        this.operation = UGEGreedOperation.LEAVE_REFUSED;
    }
    @Override
    public UGEGreedOperation getOperation() {
        return this.operation;
    }

    @Override
    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(10);
        return buffer;
    }

    @Override
    public void encodeIntoBuffer(ByteBuffer buffer) {
        buffer.putInt(10);
    }

    @Override
    public int getSize() {
        return Integer.BYTES  ;
    }
}
