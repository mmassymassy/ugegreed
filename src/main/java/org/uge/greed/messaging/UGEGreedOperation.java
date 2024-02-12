package org.uge.greed.messaging;

public enum UGEGreedOperation {

    JOIN_REQUEST(0),
    JOIN_REQUEST_BROADCAST(1),
    PEER_LIST(2),
    WORKLOAD_REQUEST(3),
    WORKLOAD_RESULT(4),
    LEAVE_REQUEST(5),
    WORKLOAD_ACK(6),
    WORKLOAD_NACK(7),

    LEAVE_GRANTED(9),
    LEAVE_REFUSED(10),
    PLEASE_RECONNECT(11),
    RECONNECTION_REQUEST(12),
    RECONECTION_ACCEPTED(13),
    RECONECTED(14),
    RECONECTION_REFUSED(15),
    LEAVING_BROADCAST(16);

    public final int value;

    private UGEGreedOperation(int value) {
        this.value = value;
    }
}
