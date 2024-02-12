package org.uge.greed.network.tcp.client;

import org.uge.greed.Helpers;
import org.uge.greed.greed.UGEGreed;
import org.uge.greed.messaging.messageReaders.MessageReader;
import org.uge.greed.messaging.messageReaders.Reader;
import org.uge.greed.messaging.messages.UGEGreedBaseMessage;
import org.uge.greed.network.tcp.TcpContext;

import java.io.*;
import java.net.*;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.util.logging.Logger;


public class Client {
    private static int BUFFER_SIZE = 10_000;
    private static Logger logger = Logger.getLogger(Client.class.getName());
    private TcpContext uniqueContext;
    private UGEGreed service;
    private InetSocketAddress addr;
    private Selector selector;
    private SocketChannel sc;
    public Client(UGEGreed service) {
        this.service = service;
    }

    public void connect() {
        try {
            addr = new InetSocketAddress(InetAddress.getByName(this.service.getParent().ipAddress), this.service.getParent().port);
            sc = SocketChannel.open();
            selector = Selector.open();
            sc = SocketChannel.open();
            sc.configureBlocking(false);
            var key = sc.register(selector, SelectionKey.OP_CONNECT);
            uniqueContext = new TcpContext(key, service);
            key.attach(uniqueContext);
            sc.connect(addr);
            System.out.println("Connexion au parent reussie!");
        } catch (Exception e) {
            System.out.println("Err: connection initial au parent avec client TCP.");
        }
    }

    public void disconnect() {
        try {
            sc.close();
        } catch (IOException e) {
            // pass
        }
    }

    public void clientLoop() {
        try {
            selector.select(this::treatKey, 200);
        } catch (IOException ioe) {
            System.out.println("Err: dans la selection des cle de client TCP!");
        }
    }

    public void sendMessage(UGEGreedBaseMessage message) {
        this.uniqueContext.queueMessage(message);
    }

    private void treatKey(SelectionKey key) {
        try {
            if (key.isConnectable()) {
                uniqueContext.doConnect(this.service.getClientWasInitiated());
                this.service.setClientWasInitiated(true);
            }
        } catch (Exception e) {
            System.out.println("Err: connection au parent echoue!");
        }
        try {
            if (key.isValid() && key.isReadable()) {
                uniqueContext.doRead();
            }
            if (key.isValid() && key.isWritable()) {
                System.out.println("writing BufferOut to client socket");
                uniqueContext.doWrite();
            }
            if(!key.isValid()){
                System.out.println("Err: la cle n'est pas valide!");
            }
        } catch (IOException ecp) {
            System.out.println("Err: erreur dans la connection au parent!");
            ecp.printStackTrace();
        }
    }
}