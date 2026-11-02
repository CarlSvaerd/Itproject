// Creates orders
// Sends them to the API

package simulator;

import engine.domain.Order;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class OrderSimulator {


    /**
     * Generates a list of random PlaceOrderRequest objects.
     * @param count the number of random orders to generate.
     * @return an ArrayList of PlaceOrderRequest objects.
     */
    public ArrayList<Order> generateRandomOrders(int count) {
        Random rnd = new Random();
        ArrayList<Order> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Order.Side side = rnd.nextBoolean() ? Order.Side.BUY : Order.Side.SELL;
            UUID orderId = UUID.randomUUID();
            int accountId = rnd.nextInt(900000) + 100000; // 6 numbers
            String symbol = "SYM" + (rnd.nextInt(900) + 100); // Random symbol like SYM100 to SYM999
            double price = 100 + (200 - 100) * rnd.nextDouble(); // Random price between 100 and 200
            int quantity = rnd.nextInt(100) + 1; // Random quantity between 1 and 100
            orders.add(new Order(orderId, accountId, symbol, side, price, quantity, quantity, Instant.now()));
        }
        return orders;
    }
}
