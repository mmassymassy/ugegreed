package org.uge.greed.messaging.messages;

import org.uge.greed.Helpers;
import org.uge.greed.messaging.UGEGreedOperation;

import java.nio.ByteBuffer;

public class UGEGreedJoinRequestBroadcastBaseMessage implements UGEGreedBaseMessage {
    public String joinerAddress ;
    public int joinerAddressInt ;
    public int joinderPort ;
    public String senderAddress;
    public int senderAddressInt ;
    public int senderPort ;
    public UGEGreedOperation operation ;


    public UGEGreedJoinRequestBroadcastBaseMessage(String originalAddress, int originalPort, String senderAddress, int senderPort){
        this.senderAddress = senderAddress ;
        this.senderPort = senderPort ;
        this.senderAddressInt = Helpers.stringToIntAddress(senderAddress);

        this.joinerAddress = originalAddress ;
        this.joinerAddressInt = Helpers.stringToIntAddress(originalAddress);

        this.joinderPort = originalPort;
        this.operation = UGEGreedOperation.JOIN_REQUEST_BROADCAST ;
    }

    public UGEGreedJoinRequestBroadcastBaseMessage(int originalAddress, int originalPort, int senderAddress, int senderPort){
        this.senderAddress = Helpers.intToStringAddress(senderAddress) ;
        this.senderPort = senderPort ;
        this.senderAddressInt = (senderAddress);

        this.joinerAddressInt = originalAddress ;
        this.joinerAddress = Helpers.intToStringAddress(originalAddress);

        this.joinderPort = originalPort;

        this.operation = UGEGreedOperation.JOIN_REQUEST_BROADCAST ;
    }

    @Override
    public UGEGreedOperation getOperation() {
        return operation;
    }

    public ByteBuffer encode(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer
                .putInt(1)
                .putInt(joinerAddressInt)
                .putInt(joinderPort)
                .putInt(senderAddressInt)
                .putInt(senderPort);
        return buffer;
    }

    public void encodeIntoBuffer(ByteBuffer buffer) {
        buffer
                .putInt(1)
                .putInt(joinerAddressInt)
                .putInt(joinderPort)
                .putInt(senderAddressInt)
                .putInt(senderPort);
    }

    public int getSize(){
        return Integer.BYTES * 5;
    }

}
