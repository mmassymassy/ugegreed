package org.uge.greed.network.tcp;

import org.uge.greed.greed.UGEGreed;
import org.uge.greed.messaging.messageReaders.MessageReader;
import org.uge.greed.messaging.messageReaders.Reader;
import org.uge.greed.messaging.messages.UGEGreedBaseMessage;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;

public class TcpContext {
    private static final int BUFFER_SIZE = 1024;

    private final SelectionKey key;
    private final SocketChannel sc;
    private final MessageReader messageReader = new MessageReader();

    private ByteBuffer bufferIn = ByteBuffer.allocate(BUFFER_SIZE * 10);
    private ByteBuffer bufferOut = ByteBuffer.allocate(BUFFER_SIZE * 10);
    private boolean closed = false;

    private UGEGreed service;


    private final ArrayDeque<UGEGreedBaseMessage> queue = new ArrayDeque<>();

    public TcpContext(SelectionKey key, UGEGreed service) {
        this.key = key;
        this.sc = (SocketChannel) key.channel();
        this.service = service;
    }

    /*
     * Process the content of bufferIn
     *
     * The convention is that bufferIn is in write-mode before the call to process
     * and after the call
     *
     */
    private void processIn() {
        for (; ; ) {
            Reader.ProcessStatus status = messageReader.process(bufferIn);
            switch (status) {
                case DONE:
                    var message = messageReader.get();
                    service.handleMessageIn(message, key);
                    messageReader.reset();
                    break;
                case REFILL:
                    return;
                case ERROR:
                    System.out.println("Err: while reading message");
                    silentlyClose();
                    return;
            }
        }

    }

    /**
     * Add a message to the message queue, tries to fill bufferOut and
     * updateInterestOps
     *
     * @param msg
     */
    public void queueMessage(UGEGreedBaseMessage msg) {
        // TODO
        queue.offer(msg);
        processOut();
        try {
            updateInterestOps();
        } catch (CancelledKeyException e) {
            silentlyClose();
        }
    }

    /**
     * Try to fill bufferOut from the message queue
     */
    private void processOut() {
        if (queue.isEmpty()) {
            return;
        }

        UGEGreedBaseMessage messageInQueue = queue.peek();

        try {
            messageInQueue.encodeIntoBuffer(bufferOut);
            queue.poll();
        } catch (BufferOverflowException e) {
            e.printStackTrace();
            return;
        }
    }

    /**
     * Update the interestOps of the key looking only at values of the boolean
     * closed and of both ByteBuffers.
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * updateInterestOps and after the call. Also it is assumed that process has
     * been be called just before updateInterestOps.
     */
    private void updateInterestOps() throws CancelledKeyException {
        // TODO
        var interestOps = 0;

        if (bufferOut.position() != 0) {
            interestOps |= SelectionKey.OP_WRITE;
        }

        if (!closed && bufferIn.hasRemaining()) {
            interestOps |= SelectionKey.OP_READ;
        }

        if (interestOps == 0) {
            System.out.println("Err: interestOps == 0");
            silentlyClose();
            return;
        }
        key.interestOps(interestOps);
    }

    private void silentlyClose() {
        try {
            sc.close();
        } catch (IOException e) {
            // ignore exception
        }
    }

    /**
     * Performs the read action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doRead and after the call
     *
     * @throws IOException
     */
    public void doRead() throws IOException {
        System.out.println("reading ...");
        var readBytes = sc.read(bufferIn);

        if (readBytes == -1) {
            closed = true;
        }

        processIn();
        try {
            updateInterestOps();
        } catch (CancelledKeyException e) {
            silentlyClose();
        }
    }

    /**
     * Performs the write action on sc
     * <p>
     * The convention is that both buffers are in write-mode before the call to
     * doWrite and after the call
     *
     * @throws IOException
     */

    public void doWrite() throws IOException {
        bufferOut.flip();
        sc.write(bufferOut);
        bufferOut.compact();
        processOut();
        try {
            updateInterestOps();
        } catch (CancelledKeyException e) {
            silentlyClose();
        }
    }

    public void doConnect(Boolean clientWasIntiated) throws IOException, ConnectException {
        // TODO
        System.out.println("info: doConnect");
        if (!sc.finishConnect()) {
            return;
        }
        key.interestOps(SelectionKey.OP_READ);

        service.sendJoinRequest();

        if(clientWasIntiated){
            this.service.setState(UGEGreed.UGEGReedStat.JOINED);
        }
    }
}
