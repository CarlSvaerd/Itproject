// POST /orders

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
    private final MatchingEngine matchingEngine;

    public Orders(MatchingEngine matchingEngine)
    {
        this.matchingEngine = matchingEngine;
    }

    public void handlePost(HttpExchange exchange) throws IOException
    {
        boolean isPost = "POST".equals(exchange.getRequestMethod());
        if (isPost == false)
        {
            writeResponse(exchange, 405, "Use POST\n");
            return;
        }

        placeOrder(exchange);
    }


    private void placeOrder(HttpExchange exchange) throws IOException
    {
        byte[] requestBytes = exchange.getRequestBody().readAllBytes();
        String orderJson = new String(requestBytes, StandardCharsets.UTF_8);
        try {
            Order order = OrdersJsonParser.parseNewOrder(orderJson);
            MatchResult result = matchingEngine.place(order);
            if (result.getAck().getStatus() == OrderAck.Status.REJECTED) {
                writeResponse(exchange, 400, JsonSerializer.orderAckToJson(result.getAck()));
                return;
            }
            writeResponse(exchange, 200, JsonSerializer.orderAckToJson(result.getAck()));
        } catch (IllegalArgumentException | DateTimeException e) {
            writeResponse(exchange, 400, JsonSerializer.errorToJson("Invalid order"));
        }
    }

    private static void writeResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException
    {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream responseBody = exchange.getResponseBody())
        {
            responseBody.write(responseBytes);
        }
    }
}
