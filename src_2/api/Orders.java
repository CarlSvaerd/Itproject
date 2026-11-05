// This is the door for /orders.
//
// POST:
// 1. Read the request's body as text.
// 2. Translate JSON to a Order object with the OrdersJsonParser.
// 3. Call matchingEngine.place(order)
// 4. Write back a ack as JSON.
//
// GET:
// 1. Read data/orders.json, or return an empty list if it does not exist yet.
// 2. Send back the resting orders with status 200.
//
// Example of a call:
// curl -X POST http://localhost:8080/orders \
//  -H "Content-Type: application/json" \
//  -d '{"order_id":"...","account_id":100001,"symbol":"AAPL","side":"buy","price":100,"quantity":10}'
//
// curl http://localhost:8080/orders

package api;

import com.sun.net.httpserver.HttpExchange;
import engine.MatchingEngine;
import engine.domain.MatchResult;
import engine.domain.Order;
import engine.domain.OrderAck;
import engine.domain.OrdersJsonParser;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import serialization.JsonSerializer;

public class Orders
{
    // Same engine instance for every POST, so the order book stays alive
    private final MatchingEngine matchingEngine;

    public Orders(MatchingEngine matchingEngine)
    {
        this.matchingEngine = matchingEngine;
    }

    public void handle(HttpExchange request) throws IOException
    {
        String method = request.getRequestMethod();
        if ("GET".equals(method))
        {
            getOrders(request);
            return;
        }
        
        if ("POST".equals(method))
        {
            placeOrder(request);
            return;
        }

        writeResponse(request, 405, "Use GET or POST\n");
    }

    private void getOrders(HttpExchange request) throws IOException
    {
        // Resting orders are written by MatchingEngine. Before the first order the file does not exist.
        Path ordersFile = Path.of("data", "orders.json");
        if (!Files.exists(ordersFile))
        {
            writeResponse(request, 200, "[]\n");
            return;
        }

        byte[] ordersFileBytes = Files.readAllBytes(ordersFile);
        String ordersJson = new String(ordersFileBytes, StandardCharsets.UTF_8);
        
        writeResponse(request, 200, ordersJson);
    }

    private void placeOrder(HttpExchange request) throws IOException
    {
        // Read the raw JSON body from the client
        byte[] requestBytes = request.getRequestBody().readAllBytes();
        String orderJson = new String(requestBytes, StandardCharsets.UTF_8);

        try {
            // Turn JSON into an Order, then let the engine validate and match it
            Order order = OrdersJsonParser.parseNewOrder(orderJson);
            MatchResult result = matchingEngine.place(order); // Do something with the result, e.g., log it or send it back to the client

            if (result.getAck().getStatus() == OrderAck.Status.REJECTED) {
                // Engine rejected the order, for example, bad fields and so on.
                writeResponse(request, 400, JsonSerializer.orderAckToJson(result.getAck()));
                return;
            }

            // Engine accepted the order so send the acknowledgement back
            writeResponse(request, 200, JsonSerializer.orderAckToJson(result.getAck()));

        } catch (IllegalArgumentException | DateTimeException e) {
            // JSON could not be parsed into a valid order
            writeResponse(request, 400, JsonSerializer.errorToJson("Invalid order"));
        }
    }

    // Send status code and body back to the client
    private static void writeResponse(HttpExchange request, int statusCode, String responseText) throws IOException
    {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);

        request.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        request.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream responseBody = request.getResponseBody())
        {
            responseBody.write(responseBytes);
        }
    }
}
