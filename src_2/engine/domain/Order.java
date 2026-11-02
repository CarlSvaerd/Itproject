// Incoming order

package engine.domain;

import java.time.Instant;
import java.util.UUID;

public class Order {
    public enum Side { BUY, SELL }

    private final UUID orderId;
    private final int accountId; // 6 siffror
    private final String symbol;
    private final Side side;
    private final double price;
    private final int quantity;
    private final int remainingQuantity;
    private final Instant timestamp;

    public Order(UUID orderId, int accountId, String symbol, Side side, double price, int quantity,
                 int remainingQuantity, Instant timestamp) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
        this.remainingQuantity = remainingQuantity;
        this.timestamp = timestamp;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getSymbol() {
        return symbol;
    }

    public Side getSide() {
        return side;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    
}
