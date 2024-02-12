package org.uge.greed.demo.messageReaderTest;

import org.uge.greed.Helpers;
import org.uge.greed.messaging.messageReaders.MessageReader;
import org.uge.greed.messaging.messages.*;
import org.uge.greed.network.tcp.server.ServerConfig;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class MessageReaderTest {
    public static void main(String[] args){
        int address = Helpers.stringToIntAddress("127.0.0.1");
        int port = 8080 ;

        int senderAddress = Helpers.stringToIntAddress("127.0.0.1");
        int senderPort = 9090;

        UGEGreedWorkloadAckMessage workload = new UGEGreedWorkloadAckMessage(10, new ServerConfig(senderAddress,senderPort), new ServerConfig(address,port));

        var buffer = workload.encode();

        MessageReader messageReader = new MessageReader();

        var status = messageReader.process(buffer);

        var decoded = messageReader.get();

    }
}
