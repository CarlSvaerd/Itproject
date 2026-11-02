package simulator;

import api.PlaceOrderRequest;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;


public class OrderSimulator {

    public ArrayList<PlaceOrderRequest> generateRandomOrders(int count) {
        Random rnd = new Random();
        ArrayList<PlaceOrderRequest> orders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            PlaceOrderRequest.Side side = rnd.nextBoolean() ? PlaceOrderRequest.Side.BUY : PlaceOrderRequest.Side.SELL;
            UUID orderId = UUID.randomUUID();
            int accountId = rnd.nextInt(900000) + 100000; // 6 numbers
            String symbol = "SYM" + (rnd.nextInt(900) + 100); // Random symbol like SYM100 to SYM999
            double price = 100 + (200 - 100) * rnd.nextDouble(); // Random price between 100 and 200
            int quantity = rnd.nextInt(100) + 1; // Random quantity between 1 and 100
            orders.add(new PlaceOrderRequest(orderId, accountId, symbol, side, price, quantity));
        }
        return orders;
    }
}
