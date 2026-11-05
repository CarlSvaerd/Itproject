package engine.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Order – a limit order placed in the market (spec section 1).
 *
 * Lifecycle:
 *   1. Created by the simulator or parsed from POST /orders.
 *   2. Validated and matched by MatchingEngine / OrderBook.
 *   3. If not fully filled, it rests in the OrderBook until a counterparty arrives.
 *
 * All fields are fixed except remainingQuantity, which goes down
 * each time the order takes part in a trade (0 = fully filled).
 */
public class Order {
    public enum Side { BUY, SELL }

    private final UUID orderId;           // unique id for the order
    private final int accountId;          // account placing the order (6 digits)
    private final String symbol;          // instrument, e.g. "AAPL"
    private final Side side;              // BUY or SELL
    private final double price;           // limit price: max to pay (buy) or min to accept (sell)
    private final int quantity;           // original size, always > 0
    private int remainingQuantity;        // what is still unfilled; starts equal to quantity
    private final Instant timestamp;      // when the order was created; used for time priority

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

    /**
     * Called by OrderBook.match() after each trade.
     * Note: there is no check that the result stays >= 0 –
     * match() makes sure filledQuantity is never larger than what is left.
     */
    public void reduceRemainingQuantity(int filledQuantity) {
        remainingQuantity = remainingQuantity - filledQuantity;
    }
}