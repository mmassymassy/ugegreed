package org.uge.greed.messaging.messages;

import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class UGEGreedWorkloadResult implements UGEGreedBaseMessage{
    private ServerConfig destination;
    private ServerConfig source;
    private UGEGreedOperation operation ;
    private int workloadId;
    private String workloadResult;
    private Long calculatedValue;
    public UGEGreedWorkloadResult(ServerConfig source, ServerConfig destination,int workloadId,String workloadResult, Long calculatedValue ){
        this.operation = UGEGreedOperation.WORKLOAD_RESULT ;
        this.destination = destination;
        this.workloadId = workloadId;
        this.workloadResult = workloadResult != null ? workloadResult : "" ;
        this.source = source;
        this.calculatedValue = calculatedValue;
    }

    public ServerConfig getDestination() {
        return destination;
    }
    public ServerConfig getSource(){return source; }

    public int getWorkloadId() {
        return workloadId;
    }

    public String getWorkloadResult() {
        return workloadResult;
    }
    public Long getCalculatedValue() {
        return calculatedValue;
    }

    @Override
    public UGEGreedOperation getOperation() {
        return operation;
    }


    public ByteBuffer encode(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);

        ByteBuffer resultBytes = StandardCharsets.UTF_8.encode(this.workloadResult.toString());

        buffer.putInt(4)
                .putInt(source.ipAddressInt)
                .putInt(source.port)
                .putInt(destination.ipAddressInt)
                .putInt(destination.port)
                .putInt(workloadId)
                .putInt(resultBytes.remaining())
                .put(resultBytes)
                .putLong(this.calculatedValue);


        return buffer;
    }

    public void encodeIntoBuffer(ByteBuffer buffer){
        ByteBuffer resultBytes = StandardCharsets.UTF_8.encode(this.workloadResult.toString());

        buffer.putInt(4)
                .putInt(source.ipAddressInt)
                .putInt(source.port)
                .putInt(destination.ipAddressInt)
                .putInt(destination.port)
                .putInt(workloadId)
                .putInt(resultBytes.remaining())
                .put(resultBytes)
                .putLong(this.calculatedValue);
    }

    public int getSize(){
        if(workloadResult == null){
            return  36;
        }else{
            return  36+ workloadResult.getBytes(StandardCharsets.UTF_8).length ;
        }
    }
}
