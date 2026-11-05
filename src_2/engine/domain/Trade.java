package engine.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Trade – one execution between a buy order and a sell order.
 *
 * Matches spec section 3 (fill/trade event) and section 4 (history row):
 * the same object is returned to the caller when it happens and stored
 * in data/trades.json as part of the trade history.
 *
 * Created by OrderBook.match(). One incoming order can produce several trades
 * if it is filled against more than one resting order (partial fills).
 *
 * Immutable: a trade never changes after it has happened.
 */
public class Trade {
    private final UUID tradeId;            // unique id for this execution
    private final String symbol;           // instrument, e.g. "AAPL"
    private final double price;            // execution price = the resting order's price
    private final int quantity;            // units traded in this execution (can be a partial fill)
    private final UUID buyOrderId;         // buy order that took part
    private final int buyAccountId;        // account of the buy order (6 digits)
    private final UUID sellOrderId;        // sell order that took part
    private final int sellAccountId;       // account of the sell order (6 digits)
    private final Instant timestamp;       // when the match happened
    private final int remainingQuantity;   // what is left of the INCOMING order after this trade (0 = fully filled)

    public Trade(UUID tradeId, String symbol, double price, int quantity,
                 UUID buyOrderId, int buyAccountId,
                 UUID sellOrderId, int sellAccountId,
                 Instant timestamp, int remainingQuantity) {
        this.tradeId = tradeId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.buyOrderId = buyOrderId;
        this.buyAccountId = buyAccountId;
        this.sellOrderId = sellOrderId;
        this.sellAccountId = sellAccountId;
        this.timestamp = timestamp;
        this.remainingQuantity = remainingQuantity;
    }

    public UUID getTradeId() {
        return tradeId;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public UUID getBuyOrderId() {
        return buyOrderId;
    }

    public int getBuyAccountId() {
        return buyAccountId;
    }

    public UUID getSellOrderId() {
        return sellOrderId;
    }

    public int getSellAccountId() {
        return sellAccountId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }
}