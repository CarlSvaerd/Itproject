package engine.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * OrderBook – holds all active (resting) orders and performs the matching.
 *
 * Structure:
 *   - Two lists, one per side, always kept sorted by price-time priority:
 *       buyOrders:  highest price first, oldest first at equal price
 *       sellOrders: lowest price first,  oldest first at equal price
 *   - The first order in each list is therefore always the best one to trade with.
 *
 * Matching (see match()):
 *   - An incoming order is matched against the opposite side, best price first.
 *   - Only orders with the same symbol can match.
 *   - Trades execute at the resting order's price.
 *   - Partial fills are allowed; fully filled resting orders are removed.
 *
 */
public class OrderBook {
    private final List<Order> buyOrders = new ArrayList<>();
    private final List<Order> sellOrders = new ArrayList<>();

    /**
     * Creates the book and restores active orders from data/orders.json.
     */
    public OrderBook() {
        loadOrdersFromJson();
    }

    private void loadOrdersFromJson() {
        // Look in data/ first, then in exchange-simulator/data/ (when run from the parent folder)
        Path ordersPath = Path.of("data", "orders.json");
        if (!Files.exists(ordersPath)) {
            ordersPath = Path.of("exchange-simulator", "data", "orders.json");
        }
        // No file yet means a fresh market – start with an empty book
        if (!Files.exists(ordersPath)) {
            return;
        }
        try {
            for (Order order : OrdersJsonParser.parse(Files.readString(ordersPath))) {
                this.addOrder(order);
            }
        } catch (IOException | IllegalArgumentException | DateTimeException e) {
            throw new IllegalStateException("Unable to load orders from " + ordersPath, e);
        }
    }

    /**
     * Adds an order to its side of the book and keeps that side sorted.
     * Note: this does NOT match – call match() first for incoming orders.
     */
    public void addOrder(Order order) {
        if (order.getSide() == Order.Side.BUY) {
            buyOrders.add(order);
            // Highest price first; at equal price, oldest first
            buyOrders.sort(Comparator.comparing(Order::getPrice).reversed()
                    .thenComparing(Order::getTimestamp));
        } else {
            sellOrders.add(order);
            // Lowest price first; at equal price, oldest first
            sellOrders.sort(Comparator.comparing(Order::getPrice)
                    .thenComparing(Order::getTimestamp));
        }
    }

    /**
     * Matches an incoming order against the opposite side of the book.
     * Reduces remaining quantity on both orders and removes fully filled resting orders.
     *
     * @param incoming the new order (not yet in the book)
     * @return the trades that were created, in the order they happened (may be empty)
     */
    public List<Trade> match(Order incoming) {
        List<Trade> trades = new ArrayList<>();
        boolean isBuy = incoming.getSide() == Order.Side.BUY;
        List<Order> opposite = isBuy ? sellOrders : buyOrders;

        // Iterator is used so filled orders can be removed safely while looping
        Iterator<Order> it = opposite.iterator();
        while (incoming.getRemainingQuantity() > 0 && it.hasNext()) {
            Order resting = it.next();

            // Different symbol – skip, but keep looking
            if (!resting.getSymbol().equals(incoming.getSymbol())) {
                continue;
            }

            // Buy: seller's price must be <= our limit. Sell: buyer's price must be >= our limit.
            boolean priceOk = isBuy
                    ? resting.getPrice() <= incoming.getPrice()
                    : resting.getPrice() >= incoming.getPrice();
            if (!priceOk) {
                break; // list is sorted, so every remaining order has a worse price
            }

            // Trade as much as both sides allow (partial fill if one side is larger)
            int qty = Math.min(incoming.getRemainingQuantity(), resting.getRemainingQuantity());
            incoming.reduceRemainingQuantity(qty);
            resting.reduceRemainingQuantity(qty);

            Order buy = isBuy ? incoming : resting;
            Order sell = isBuy ? resting : incoming;

            trades.add(new Trade(
                    UUID.randomUUID(),
                    incoming.getSymbol(),
                    resting.getPrice(),               // trade executes at the resting order's price
                    qty,
                    buy.getOrderId(), buy.getAccountId(),
                    sell.getOrderId(), sell.getAccountId(),
                    Instant.now(),
                    incoming.getRemainingQuantity())); // remaining_quantity refers to the incoming order

            if (resting.getRemainingQuantity() == 0) {
                it.remove(); // fully filled – remove from the book
            }
        }
        return trades;
    }

    public List<Order> getBuyOrders() {
        return buyOrders;
    }

    public List<Order> getSellOrders() {
        return sellOrders;
    }
}