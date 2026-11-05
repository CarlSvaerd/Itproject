// This is the door for POST /orders.
//
// 1. Check that it is post.
// 2. Read the request's body as text.
// 3. Translate JSON to a Order object with the OrdersJsonParser.
// 4. Call matchingEngine.place(order)
// 5. Write back a ack as JSON.
//
// Example of a call:
// curl -X POST http://localhost:8080/orders \
//  -H "Content-Type: application/json" \
//  -d '{"order_id":"...","account_id":100001,"symbol":"AAPL","side":"buy","price":100,"quantity":10}'

package api;

import com.sun.net.httpserver.HttpExchange;
import engine.MatchingEngine;
import engine.domain.MatchResult;
import engine.domain.Order;
import engine.domain.OrdersJsonParser;
import engine.domain.OrderAck;
import serialization.JsonSerializer;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;

public class Orders
{
    // Same engine instance for every POST, so the order book stays alive
    private final MatchingEngine matchingEngine;

    public Orders(MatchingEngine matchingEngine)
    {
        this.matchingEngine = matchingEngine;
    }

    public void handlePost(HttpExchange request) throws IOException
    {
        // Reject anything that is not POST
        boolean isPost = "POST".equals(request.getRequestMethod());
        if (isPost == false)
        {
            writeResponse(request, 405, "Use POST\n");
            return;
        }

        placeOrder(request);
    }

    private void placeOrder(HttpExchange request) throws IOException
    {
        // Read the raw JSON body from the client
        byte[] requestBytes = request.getRequestBody().readAllBytes();
        String orderJson = new String(requestBytes, StandardCharsets.UTF_8);

        try {
            // Turn JSON into an Order, then let the engine validate and match it
            Order order = OrdersJsonParser.parseNewOrder(orderJson);
            MatchResult result = matchingEngine.place(order);

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
