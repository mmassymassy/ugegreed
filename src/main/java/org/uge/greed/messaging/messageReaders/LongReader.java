package org.uge.greed.messaging.messageReaders;

import java.nio.ByteBuffer;

public class LongReader implements Reader<Long>{
    private enum State {
        DONE, WAITING, ERROR
    };
    private LongReader.State state = LongReader.State.WAITING;
    private final ByteBuffer internalBuffer = ByteBuffer.allocate(Long.BYTES); // write-mode
    private long value;

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == LongReader.State.DONE || state == LongReader.State.ERROR) {
            throw new IllegalStateException();
        }
        buffer.flip();
        try {
            if (buffer.remaining() <= internalBuffer.remaining()) {
                internalBuffer.put(buffer);
            } else {
                int oldLimit = buffer.limit();
                buffer.limit(internalBuffer.remaining());
                internalBuffer.put(buffer);
                buffer.limit(oldLimit);
            }
        } finally {
            buffer.compact();
        }
        if (internalBuffer.hasRemaining()) {
            return ProcessStatus.REFILL;
        }
        state = LongReader.State.DONE;
        internalBuffer.flip();
        value = internalBuffer.getLong();
        return ProcessStatus.DONE;
    }

    @Override
    public Long get() {
        if (state != LongReader.State.DONE) {
            throw new IllegalStateException();
        }
        return value;
    }

    @Override
    public void reset() {
        state = LongReader.State.WAITING;
        internalBuffer.clear();
    }
}
