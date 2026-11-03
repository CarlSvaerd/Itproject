package engine.domain;

// Någon vill köpa 10 AAPL för 100. Det finns ingen säljare till det priset, så ordern läggs i boken.

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.time.Instant;
import java.util.Iterator;
import java.util.UUID;

public class OrderBook {
    private final List<Order> buyOrders = new ArrayList<>();
    private final List<Order> sellOrders = new ArrayList<>();


    /**
     * Loads orders from data/orders.json.
     */
    public OrderBook() {
        loadOrdersFromJson();
    }

    private void loadOrdersFromJson() {
        Path ordersPath = Path.of("data", "orders.json"); // Default path for orders.json
        if (!Files.exists(ordersPath)) {
            ordersPath = Path.of("exchange-simulator", "data", "orders.json");
        }
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

    public void addOrder(Order order) {
        if (order.getSide() == Order.Side.BUY) {
            buyOrders.add(order);
            // Högst pris först, vid lika pris äldst först
            buyOrders.sort(Comparator.comparing(Order::getPrice).reversed()
                    .thenComparing(Order::getTimestamp));
        } else {
            sellOrders.add(order);
            // Lägst pris först, vid lika pris äldst först
            sellOrders.sort(Comparator.comparing(Order::getPrice)
                    .thenComparing(Order::getTimestamp));
        }
    }

    /**
     * Matchar en inkommande order mot motsatt sida i boken.
     * Returnerar de avslut som blev. Fyllda ordrar tas bort från boken.
     */
    public List<Trade> match(Order incoming) {
        List<Trade> trades = new ArrayList<>();
        boolean isBuy = incoming.getSide() == Order.Side.BUY;
        List<Order> opposite = isBuy ? sellOrders : buyOrders;

        Iterator<Order> it = opposite.iterator();
        while (incoming.getRemainingQuantity() > 0 && it.hasNext()) {
            Order resting = it.next();

            if (!resting.getSymbol().equals(incoming.getSymbol())) {
                continue;
            }

            boolean priceOk = isBuy
                    ? resting.getPrice() <= incoming.getPrice()
                    : resting.getPrice() >= incoming.getPrice();
            if (!priceOk) {
                break; // listan är sorterad, så resten är ännu sämre
            }

            int qty = Math.min(incoming.getRemainingQuantity(), resting.getRemainingQuantity());
            incoming.reduceRemainingQuantity(qty);
            resting.reduceRemainingQuantity(qty);

            Order buy = isBuy ? incoming : resting;
            Order sell = isBuy ? resting : incoming;

            trades.add(new Trade(
                    UUID.randomUUID(),
                    incoming.getSymbol(),
                    resting.getPrice(),        // avslut på priset som låg i boken
                    qty,
                    buy.getOrderId(), buy.getAccountId(),
                    sell.getOrderId(), sell.getAccountId(),
                    Instant.now(),
                    incoming.getRemainingQuantity()));

            if (resting.getRemainingQuantity() == 0) {
                it.remove(); // helt fylld, bort från boken
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