// Starts the market
// java Main 10 - generates 10 orders and writes orders.json and trades.json
// java Main --simulate [duration-seconds] [interval-millis] - runs an HTTP order event demo

// java Main                 → Start only a http server
// java Main 10              → Make 10 orders and add them to the matchingEningne.
// java Main --simulate ...  → Start the server and send orders via http.

import com.sun.net.httpserver.HttpServer;
import java.util.ArrayList;
import java.net.URI;
import java.time.Duration;

import api.Exchange;
import engine.MatchingEngine;
import engine.domain.MatchResult;
import engine.domain.Order;
import simulator.OrderSimulator;

public class Main {
    public static void main(String[] args) throws Exception
    {
        // Mode 1 - HTTP demo that posts random orders for a while
        if (args.length > 0 && "--simulate".equals(args[0]))
        {
            runOrderEventDemo(args);
            return;
        }

        // Mode 2 - generate N orders and place them without HTTP
        boolean generateOrders = args.length == 1;
        if (generateOrders)
        {
            int orderCount = Integer.parseInt(args[0]);
            ArrayList<Order> generatedOrders = new OrderSimulator().generateRandomOrders(orderCount);
            MatchingEngine matchingEngine = new MatchingEngine();

            for (Order generatedOrder : generatedOrders)
            {
                MatchResult result = matchingEngine.place(generatedOrder);
                System.out.println(result.getAck().getStatus() + " " + generatedOrder.getOrderId()
                        + " trades=" + result.getTrades().size());
            }
            return;
        }

        // Mode 3 - Just start listening for HTTP requests
        new Exchange().start();
    }

    private static void runOrderEventDemo(String[] args) throws Exception
    {
        if (args.length > 3)
        {
            throw new IllegalArgumentException("Usage: java Main --simulate [duration-seconds] [interval-millis]");
        }
        long durationSeconds = args.length > 1 ? Long.parseLong(args[1]) : 10;
        long intervalMillis = args.length > 2 ? Long.parseLong(args[2]) : 1000;

        // Start server, send random POSTs to /orders, then shut down
        HttpServer server = new Exchange().start();
        try
        {
            Thread sender = new OrderSimulator().sendRandomOrderEvents(
                    URI.create("http://localhost:8080/orders"),
                    Duration.ofSeconds(durationSeconds),
                    Duration.ofMillis(intervalMillis));
            sender.join();
            System.out.println("Random order event demo completed.");
        }
        finally
        {
            server.stop(0);
        }
    }
}
