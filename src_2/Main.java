// Starts the market
// java Main 10 - generates 10 orders and writes orders.json and trades.json

import java.util.ArrayList;

import api.Exchange;
import engine.MatchingEngine;
import engine.domain.Order;
import simulator.OrderSimulator;

public class Main {
    public static void main(String[] args) throws Exception
    {
        boolean generateOrders = args.length == 1;
        if (generateOrders)
        {
            int orderCount = Integer.parseInt(args[0]);
            ArrayList<Order> generatedOrders = new OrderSimulator().generateRandomOrders(orderCount);
            MatchingEngine matchingEngine = new MatchingEngine();

            for (Order generatedOrder : generatedOrders)
            {
                boolean accepted = matchingEngine.place(generatedOrder);
                System.out.println(accepted + " " + generatedOrder.getOrderId());
            }
            return;
        }

        new Exchange().start();
    }
}
