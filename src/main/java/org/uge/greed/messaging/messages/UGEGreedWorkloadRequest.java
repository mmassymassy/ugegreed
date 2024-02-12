package org.uge.greed.messaging.messages;

import org.uge.greed.execution.UGEGreenCheckersEnum;
import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class UGEGreedWorkloadRequest implements UGEGreedBaseMessage{
    private ServerConfig sender;
    private ServerConfig destination;
    private UGEGreedOperation operation ;

    private int workloadId;
    private long startValue;
    private long endValue;
    private String fullyQualifiedName;
    private URL jarUrl ;


    public UGEGreedWorkloadRequest(ServerConfig sender, ServerConfig destination, int workloadId, long startValue, long endValue,String fullyQualifiedName, URL jarUrl) {
        this.operation = UGEGreedOperation.WORKLOAD_REQUEST;
        this.sender = sender;
        this.destination = destination;
        this.workloadId = workloadId;
        this.startValue = startValue;
        this.endValue = endValue;
        this.fullyQualifiedName = fullyQualifiedName ;
        this.jarUrl = jarUrl;
    }

    @Override
    public UGEGreedOperation getOperation() {
        return operation;
    }

    public ByteBuffer encode(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        ByteBuffer fullyQualifiedNameBytes = StandardCharsets.UTF_8.encode(this.fullyQualifiedName);
        ByteBuffer jarUrlBytes = StandardCharsets.UTF_8.encode(this.jarUrl.toString());
        buffer.putInt(3)
                .putInt(sender.ipAddressInt)
                .putInt(sender.port)
                .putInt(destination.ipAddressInt)
                .putInt(destination.port)
                .putInt(workloadId)
                .putLong(startValue)
                .putLong(endValue)
                .putInt(fullyQualifiedNameBytes.remaining())
                .put(fullyQualifiedNameBytes)
                .putInt(jarUrlBytes.remaining())
                .put(jarUrlBytes);

        return buffer;
    }

    public void encodeIntoBuffer(ByteBuffer buffer){
        ByteBuffer fullyQualifiedNameBytes = StandardCharsets.UTF_8.encode(this.fullyQualifiedName);
        ByteBuffer jarUrlBytes = StandardCharsets.UTF_8.encode(this.jarUrl.toString());
        buffer.putInt(3)
                .putInt(sender.ipAddressInt)
                .putInt(sender.port)
                .putInt(destination.ipAddressInt)
                .putInt(destination.port)
                .putInt(workloadId)
                .putLong(startValue)
                .putLong(endValue)
                .putInt(fullyQualifiedNameBytes.remaining())
                .put(fullyQualifiedNameBytes)
                .putInt(jarUrlBytes.remaining())
                .put(jarUrlBytes);
    }

    public int getSize(){
        return Integer.BYTES * 6 + // opertaion, sender IP, sender port, destination ip, destination port, workload id
                Long.BYTES * 2 + // start value, end value
                Integer.BYTES + // fullyQualifiedName size
                fullyQualifiedName.getBytes(StandardCharsets.UTF_8).length + // qualified name string
                Integer.BYTES + // jar url size
                jarUrl.toString().getBytes(StandardCharsets.UTF_8).length ;
    }

    public ServerConfig getSender() {
        return sender;
    }

    public ServerConfig getDestination() {
        return destination;
    }

    public int getWorkloadId() {
        return workloadId;
    }

    public long getStartValue() {
        return startValue;
    }

    public long getEndValue() {
        return endValue;
    }

    public String getFullyQualifiedName(){
        return fullyQualifiedName;
    }
    public URL getJarUrl(){
        return this.jarUrl;
    }

    @Override
    public String toString() {
        return "UGEGreedWorkloadRequest{" +
                "sender=" + sender +
                ", destination=" + destination +
                ", operation=" + operation +
                ", workloadId=" + workloadId +
                ", startValue=" + startValue +
                ", endValue=" + endValue +
                ", fullyQualifiedName='" + fullyQualifiedName + '\'' +
                ", jarUrl=" + jarUrl +
                '}';
    }
}

