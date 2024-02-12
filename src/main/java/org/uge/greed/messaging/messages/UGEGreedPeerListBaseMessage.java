package org.uge.greed.messaging.messages;

import org.uge.greed.messaging.UGEGreedOperation;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.nio.ByteBuffer;
import java.util.Set;

public class UGEGreedPeerListBaseMessage implements UGEGreedBaseMessage {
    private Set<ServerConfig> peers;
    private ServerConfig sender;
    protected UGEGreedOperation operation;

    public Set<ServerConfig> getPeers() {
        return peers;
    }

    public ServerConfig getSender() {
        return sender;
    }

    public int getNumberOfPeers(){
        return this.peers.size();
    }

    public UGEGreedPeerListBaseMessage(ServerConfig sender ,Set<ServerConfig> peers){
        this.operation = UGEGreedOperation.PEER_LIST;
        this.sender = sender;
        this.peers  = peers ;
    }

    @Override
    public UGEGreedOperation getOperation() {
        return this.operation;
    }

    public ByteBuffer encode(){
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        buffer.putInt(2)
                .putInt(sender.ipAddressInt)
                .putInt(sender.port)
                .putInt(this.getNumberOfPeers());
        for(ServerConfig peer : this.peers){
            buffer.putInt(peer.ipAddressInt).putInt(peer.port);
        }

        return buffer;

    }

    public void encodeIntoBuffer(ByteBuffer buffer){
        buffer.putInt(2)
                .putInt(sender.ipAddressInt)
                .putInt(sender.port)
                .putInt(this.getNumberOfPeers());
        for(ServerConfig peer : this.peers){
            buffer.putInt(peer.ipAddressInt).putInt(peer.port);
        }
    }

    @Override
    public int getSize() {
        return Integer.BYTES * 4 + this.getNumberOfPeers() * (Integer.BYTES * 2 );
    }

}
