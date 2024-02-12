package org.uge.greed.messaging.messages;

import org.uge.greed.Helpers;
import org.uge.greed.messaging.UGEGreedOperation;

import java.nio.ByteBuffer;

public class UGEGreedJoinRequestBaseMessage implements  UGEGreedBaseMessage {

    public String address;
    public int addressInt;
    public int port;
    protected  UGEGreedOperation operation  ;

    public UGEGreedJoinRequestBaseMessage(String address, int port) {
        this.operation = UGEGreedOperation.JOIN_REQUEST;
        this.port = port;
        this.address = address;
        this.addressInt = Helpers.stringToIntAddress(address);
    }

    public UGEGreedJoinRequestBaseMessage(int address, int port) {
        this.operation = UGEGreedOperation.JOIN_REQUEST ;
        this.port = port;
        this.address = Helpers.intToStringAddress(address);
        this.addressInt = address;
    }

    @Override
    public UGEGreedOperation getOperation() {
        return this.operation;
    }

    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(0).putInt(addressInt).putInt(port);

        return buffer;
    }

    public void encodeIntoBuffer(ByteBuffer buffer) {
        buffer.putInt(0).putInt(addressInt).putInt(port);
    }

    @Override
    public int getSize() {
        return  Integer.BYTES ;
    }
}
