package engine.domain;

// Någon vill köpa 10 AAPL för 100. Det finns ingen säljare till det priset, så ordern läggs i boken.

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;


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
        try {
            for (Order order : OrdersJsonParser.parse(Files.readString(ordersPath))) {
                this.addOrder(order);
            }
        } catch (IOException | IllegalArgumentException | DateTimeException e) {
            throw new IllegalStateException("Unable to load orders from " + ordersPath, e);
        }
    }

    private void addOrder(Order order) {
        if (order.getSide() == Order.Side.BUY) {
            buyOrders.add(order);
        } else {
            sellOrders.add(order);
        }
    }

    public List<Order> getBuyOrders() {
        return buyOrders;
    }

    public List<Order> getSellOrders() {
        return sellOrders;
    }
}