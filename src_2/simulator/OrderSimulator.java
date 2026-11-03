// Creates orders
// Sends them to the API

package simulator;

import engine.domain.Order;
import serialization.JsonSerializer;
import java.time.Instant;
import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class OrderSimulator {

    private static final String[] SYMBOLS = { "AAPL", "VOLV-B", "ERIC-B" };
    private final HttpClient httpClient = HttpClient.newHttpClient();

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

    /**
     * Starts sending randomly generated orders to the HTTP API on a background thread.
     *
     * @param endpoint the API endpoint, for example http://localhost:8080/orders
     * @param duration how long to generate and send orders
     * @param interval the delay between order events
     * @return the started sender thread, which can be joined or interrupted by the caller
     */
    public Thread sendRandomOrderEvents(URI endpoint, Duration duration, Duration interval) {
        Objects.requireNonNull(endpoint, "endpoint");
        Objects.requireNonNull(duration, "duration");
        Objects.requireNonNull(interval, "interval");
        String scheme = endpoint.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Endpoint must use HTTP or HTTPS");
        }

        long durationNanos = duration.toNanos();
        long intervalNanos = interval.toNanos();
        if (durationNanos <= 0 || intervalNanos <= 0) {
            throw new IllegalArgumentException("Duration and interval must be positive");
        }

        Thread sender = new Thread(() -> {
            long start = System.nanoTime();
            while (System.nanoTime() - start < durationNanos) {
                long eventStart = System.nanoTime();
                Order order = generateRandomOrders(1).get(0);
                HttpRequest request = HttpRequest.newBuilder(endpoint)
                        .header("Content-Type", "application/json; charset=utf-8")
                        .timeout(Duration.ofSeconds(5))
                        .POST(HttpRequest.BodyPublishers.ofString(JsonSerializer.orderToJson(order)))
                        .build();

                try {
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new IOException("Order API returned HTTP " + response.statusCode()
                                + ": " + response.body());
                    }
                    System.out.println("Sent order " + order.getOrderId()
                            + ", response HTTP " + response.statusCode());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (IOException e) {
                    throw new UncheckedIOException("Failed to send order " + order.getOrderId(), e);
                }

                long remainingInterval = intervalNanos - (System.nanoTime() - eventStart);
                if (remainingInterval > 0) {
                    try {
                        long millis = remainingInterval / 1_000_000;
                        int nanos = (int) (remainingInterval % 1_000_000);
                        Thread.sleep(millis, nanos);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }
        }, "random-order-event-sender");
        sender.start();
        return sender;
    }

}