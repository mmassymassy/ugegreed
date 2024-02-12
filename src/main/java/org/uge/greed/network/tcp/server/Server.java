package org.uge.greed.network.tcp.server;

import org.uge.greed.Helpers;
import org.uge.greed.greed.UGEGreed;
import org.uge.greed.messaging.messageReaders.MessageReader;
import org.uge.greed.messaging.messageReaders.Reader;
import org.uge.greed.messaging.messages.UGEGreedBaseMessage;
import org.uge.greed.network.tcp.TcpContext;
import org.uge.greed.network.tcp.client.Client;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.Logger;

public class Server {
    private static Logger logger = Logger.getLogger(Client.class.getName());
    private MessageReader messageReader = new MessageReader();
    private Selector selector;
    private UGEGreed service;

    private ServerSocketChannel serverSocketChannel;
    private Map<String, SelectionKey> clientChannels;

    public Server(UGEGreed service) throws IOException {
        this.service = service;
        this.clientChannels = new HashMap<>();

        selector = Selector.open();

        this.serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false); // Set the server socket channel to non-blocking mode
        serverSocketChannel.socket().bind(new InetSocketAddress(this.service.getServerConfig().port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void disconnectClient(String appId){
        if(this.clientChannels.containsKey(appId)){
            System.out.println("info: deconnection du client " + appId );
            this.clientChannels.get(appId).cancel();
            this.clientChannels.remove(appId);
        }
    }

    public void sendToClient(String clientId, UGEGreedBaseMessage message) {
        SelectionKey key = clientChannels.get(clientId);

        if (key != null) {
            TcpContext keyContext = ((TcpContext) key.attachment());
            keyContext.queueMessage(message);
        }
    }

    public int sendToAllClients(UGEGreedBaseMessage message) {
        int count = 0;
        for (SelectionKey key : clientChannels.values()) {
            TcpContext keyContext = ((TcpContext) key.attachment());
            keyContext.queueMessage(message);
            count++;
        }
        return count;
    }

    public void sendToAllClientsBut(String elimiate, UGEGreedBaseMessage message) {
        for (Map.Entry<String, SelectionKey> entry : clientChannels.entrySet()) {
            String clientId = entry.getKey();

            if (clientId.equals(elimiate)) {
                continue;
            }

            SelectionKey key = entry.getValue();
            TcpContext keyContext = ((TcpContext) key.attachment());
            keyContext.queueMessage(message);

        }
    }

    private void doAccept(SelectionKey key) throws IOException {
        var serverChannel = (ServerSocketChannel) key.channel();
        var client = serverChannel.accept();

        if (client == null) {
            return;
        }

        client.configureBlocking(false);
        var clientKey = client.register(selector, SelectionKey.OP_READ);

        clientKey.attach(new TcpContext(clientKey, service));

        System.out.println("info: nouveau client connecté");
    }

    public void addNewClientChannelFromJoinRequest(String clientId, SelectionKey key) {
        this.clientChannels.put(clientId, key);
    }


    private void treatKey(SelectionKey key) {
        try {
            if (key.isValid() && key.isAcceptable()) {
                doAccept(key);
            }
        } catch (IOException ioException) {
            throw new UncheckedIOException(ioException);
        }
        try {
            if (key.isValid() && key.isWritable()) {
                ((TcpContext) key.attachment()).doWrite();
            }
            if (key.isValid() && key.isReadable()) {
                ((TcpContext) key.attachment()).doRead();
            }

        } catch (IOException e) {
            System.err.println("Err: Fermiture de connection avec le client");
            safeClose(key);
        }
    }

    private void safeClose(SelectionKey key) {
        try {
            key.channel().close();
        } catch (IOException e) {
            // DO NOTHING
        }
    }

    public void loop() {
        try {
            var s = selector.select(this::treatKey, 200);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Selector getSelector() {
        return selector;
    }
}