// Same shape as a history row (in data/trades.json), so after a trade has went through.

package engine.domain;

import java.time.Instant;
import java.util.UUID;

public class Trade {
    private final UUID tradeId;
    private final String symbol;
    private final double price;
    private final int quantity;
    private final UUID buyOrderId;
    private final int buyAccountId;
    private final UUID sellOrderId;
    private final int sellAccountId;
    private final Instant timestamp;
    private final int remainingQuantity;

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
        
        //
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
