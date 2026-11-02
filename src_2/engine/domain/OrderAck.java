// Accepted or rejected with enum and says reason.

package engine.domain;

import java.time.Instant;
import java.util.UUID;

public class OrderAck {
    public enum Status { ACCEPTED, REJECTED }

    private final UUID orderId;
    private final int accountId;
    private final Status status;
    private final String rejectReason;
    private final Instant timestamp;

    public OrderAck(UUID orderId, int accountId, Status status, String rejectReason, Instant timestamp) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.timestamp = timestamp;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public int getAccountId() {
        return accountId;
    }

    public Status getStatus() {
        return status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
