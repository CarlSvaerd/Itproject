package api;

import java.time.Instant;
import java.util.UUID;

public class OrderAcknowledgement {
    public enum Status { ACCEPTED, REJECTED }

    private final UUID orderId;
    private final int accountId;
    private final Status status;
    private final String rejectReason;
    private final Instant timestamp;

    public OrderAcknowledgement(UUID orderId, int accountId, Status status,
                                String rejectReason, Instant timestamp) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.timestamp = timestamp;
    }

}