package api;

import java.time.Instant;
import java.util.UUID;

public class TradeEvent {
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

    public TradeEvent(UUID tradeId, String symbol, double price, int quantity,
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
}