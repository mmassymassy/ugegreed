package org.uge.greed.messaging.messages;

import org.uge.greed.Helpers;
import org.uge.greed.messaging.UGEGreedOperation;

import java.nio.ByteBuffer;

public class UGEGreedLeavingBroadcast implements UGEGreedBaseMessage {
    private String leaverAddress ;
    private int leaverAddressInt ;
    private int leaverPort ;
    private String senderAddress;
    private int senderAddressInt ;
    private int senderPort ;
    private int parentAddressInt ;
    private int parentPort ;
    private String parentAddress ;
    private UGEGreedOperation operation ;

    public UGEGreedLeavingBroadcast(String originalAddress, int originalPort, String senderAddress, int senderPort, String parentAddress, int parentPort){
        this.senderAddress = senderAddress ;
        this.senderPort = senderPort ;
        this.senderAddressInt = Helpers.stringToIntAddress(senderAddress);

        this.leaverAddress = originalAddress ;
        this.leaverAddressInt = Helpers.stringToIntAddress(originalAddress);

        this.leaverPort = originalPort;

        this.parentAddress = parentAddress ;
        this.parentAddressInt = Helpers.stringToIntAddress(parentAddress);

        this.parentPort = parentPort ;
        this.operation = UGEGreedOperation.LEAVING_BROADCAST ;
    }

    public UGEGreedLeavingBroadcast(int originalAddress, int originalPort, int senderAddress, int senderPort, int parentAdressInt , int parentPort ){
        this.senderAddress = Helpers.intToStringAddress(senderAddress) ;
        this.senderPort = senderPort ;
        this.senderAddressInt = (senderAddress);

        this.leaverAddressInt = originalAddress ;
        this.leaverAddress = Helpers.intToStringAddress(originalAddress);

        this.parentAddressInt = parentAdressInt ;
        this.parentAddress = Helpers.intToStringAddress(parentAdressInt);

        this.parentPort = parentPort ;
        this.leaverPort = originalPort;

        this.operation = UGEGreedOperation.LEAVING_BROADCAST ;
    }

    public UGEGreedOperation getOperation() {
        return operation;
    }

    public ByteBuffer encode(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer
                .putInt(16)
                .putInt(leaverAddressInt)
                .putInt(leaverPort)
                .putInt(senderAddressInt)
                .putInt(senderPort)
                .putInt(parentAddressInt)
                .putInt(parentPort);
        return buffer;
    }

    public void encodeIntoBuffer(ByteBuffer buffer) {
        buffer
                .putInt(16)
                .putInt(leaverAddressInt)
                .putInt(leaverPort)
                .putInt(senderAddressInt)
                .putInt(senderPort)
                .putInt(parentAddressInt)
                .putInt(parentPort)
        ;
    }

    public String getLeaverAddress() {
        return leaverAddress;
    }

    public int getLeaverAddressInt() {
        return leaverAddressInt;
    }

    public int getLeaverPort() {
        return leaverPort;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public int getSenderAddressInt() {
        return senderAddressInt;
    }

    public int getSenderPort() {
        return senderPort;
    }

    public int getParentAddressInt() {
        return parentAddressInt;
    }

    public int getParentPort() {
        return parentPort;
    }

    public String getParentAddress() {
        return parentAddress;
    }

    public int getSize(){
        return Integer.BYTES * 7;
    }

    @Override
    public String toString() {
        return "UGEGreedLeavingBroadcast{" +
                "leaverAddress='" + leaverAddress + '\'' +
                ", leaverAddressInt=" + leaverAddressInt +
                ", leaverPort=" + leaverPort +
                ", senderAddress='" + senderAddress + '\'' +
                ", senderAddressInt=" + senderAddressInt +
                ", senderPort=" + senderPort +
                ", parentAddressInt=" + parentAddressInt +
                ", parentPort=" + parentPort +
                ", parentAddress='" + parentAddress + '\'' +
                ", operation=" + operation +
                '}';
    }
}
