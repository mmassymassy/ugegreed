package org.uge.greed.messaging.messages;

import org.uge.greed.Helpers;
import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;

public class UGEGreedLeaveRequestMessage implements UGEGreedBaseMessage {

    public String address;
    public int addressInt;
    public int port;

    protected Set<ServerConfig> children;
    protected  UGEGreedOperation operation  ;


    public UGEGreedLeaveRequestMessage(String address, int port, Set<ServerConfig> children) {
        this.operation = UGEGreedOperation.LEAVE_REQUEST;
        this.port = port;
        this.address = address;
        this.addressInt = Helpers.stringToIntAddress(address);
        this.children = children;
    }

    public UGEGreedLeaveRequestMessage(int address, int port, Set<ServerConfig> children) {
        this.operation = UGEGreedOperation.LEAVE_REQUEST;
        this.port = port;
        this.addressInt = address;
        this.address = Helpers.intToStringAddress(address);
        this.children = children ;
    }

    @Override
    public UGEGreedOperation getOperation() {
        return operation;
    }

    public ByteBuffer encode() {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(5).putInt(addressInt).putInt(port).putInt(children.size());

        for (ServerConfig child : children) {
            buffer.putInt(child.ipAddressInt).putInt(child.port);
        }

        return buffer;
    }

    public void encodeIntoBuffer(ByteBuffer buffer) {
        buffer.putInt(5).putInt(addressInt).putInt(port).putInt(children.size());

        for (ServerConfig child : children) {
            buffer.putInt(child.ipAddressInt).putInt(child.port);
        }
    }

    @Override
    public int getSize(){
        return Integer.BYTES * 3;
    }
}
