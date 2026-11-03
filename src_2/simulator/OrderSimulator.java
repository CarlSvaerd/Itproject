// Creates orders
// Sends them to the API

package simulator;

import engine.domain.Order;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class OrderSimulator {

    private static final String[] SYMBOLS = { "AAPL", "VOLV-B", "ERIC-B" };

    /**
     * Generates a list of random Order objects.
     * @param count the number of random orders to generate.
     * @return an ArrayList of Order objects.
     */
    public ArrayList<Order> generateRandomOrders(int count) {
        Random rnd = new Random();
        ArrayList<Order> orders = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Order.Side side = rnd.nextBoolean() ? Order.Side.BUY : Order.Side.SELL;
            UUID orderId = UUID.randomUUID();
            int accountId = rnd.nextInt(900000) + 100000; // 6 numbers
            String symbol = SYMBOLS[rnd.nextInt(SYMBOLS.length)]; // few symbols so orders can match
            double price = Math.round((95 + 10 * rnd.nextDouble()) * 100) / 100.0; // 95.00–105.00, two decimals
            int quantity = rnd.nextInt(100) + 1; // Random quantity between 1 and 100

            orders.add(new Order(orderId, accountId, symbol, side, price, quantity, quantity, Instant.now()));
        }

        return orders;
    }
}