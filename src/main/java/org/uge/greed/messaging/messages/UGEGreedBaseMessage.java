package org.uge.greed.messaging.messages;

import org.uge.greed.messaging.UGEGreedOperation;

import java.io.Serializable;
import java.nio.ByteBuffer;

public interface UGEGreedBaseMessage   {
    static final long BYTES = 1024;

    UGEGreedOperation getOperation();

    ByteBuffer encode();
    void encodeIntoBuffer(ByteBuffer buffer) ;
    int getSize();
}
