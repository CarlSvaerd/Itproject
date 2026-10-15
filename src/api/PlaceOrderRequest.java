package api;

import java.util.UUID;

public class PlaceOrderRequest {
    public enum Side { BUY, SELL }
    private final UUID orderId;
    private final int accountId;     // 6 siffror
    private final String symbol;
    private final Side side;
    private final double price;
    private final int quantity;

    public PlaceOrderRequest(UUID orderId, int accountId, String symbol,
                             Side side, double price, int quantity) {
        this.orderId = orderId;
        this.accountId = accountId;
        this.symbol = symbol;
        this.side = side;
        this.price = price;
        this.quantity = quantity;
    }

}