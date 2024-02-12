package org.uge.greed.messaging.messageReaders;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringReader implements Reader<String> {
    private enum State {
        DONE, WAITING, ERROR
    };
    private static final Charset UTF8 = StandardCharsets.UTF_8;
    private final IntReader intReader = new IntReader();
    private int stringBufferSize = -1;
    private ByteBuffer stringBuffer = ByteBuffer.allocate(0);
    private State state = State.WAITING;
    private String value;

    private static void fillBuffer(ByteBuffer srcBuffer, ByteBuffer dstBuffer) {
        if (srcBuffer.remaining() <= dstBuffer.remaining()) {
            dstBuffer.put(srcBuffer);
        } else {
            var oldLimit = srcBuffer.limit();
            srcBuffer.limit(srcBuffer.position() + dstBuffer.remaining());
            dstBuffer.put(srcBuffer);
            srcBuffer.limit(oldLimit);
        }
    }

    @Override
    public ProcessStatus process(ByteBuffer buffer) {
        if (state == State.DONE || state == State.ERROR) {
            throw new IllegalStateException();
        }

        if(stringBufferSize == -1) {
            var status = intReader.process(buffer);

            if(status == ProcessStatus.DONE) {
                stringBufferSize = intReader.get();

                if(stringBufferSize < 0 || stringBufferSize > 1024) {
                    return ProcessStatus.ERROR;
                }
                stringBuffer = ByteBuffer.allocate(stringBufferSize);
            }
        }
        if(stringBufferSize != -1) {
            buffer.flip();
            try {
                fillBuffer(buffer, stringBuffer);
            } finally {
                buffer.compact();
            }
        }

        if (stringBufferSize == -1 || stringBuffer.hasRemaining()) {
            return ProcessStatus.REFILL;
        }

        state = State.DONE;
        stringBuffer.flip();
        value = UTF8.decode(stringBuffer).toString();

        return ProcessStatus.DONE;
    }

    @Override
    public String get() {
        if (state != State.DONE) {
            throw new IllegalStateException();
        }

        return value;
    }

    @Override
    public void reset() {
        state = State.WAITING;
        intReader.reset();
        stringBufferSize = -1;
        stringBuffer = ByteBuffer.allocate(0);
    }
}
