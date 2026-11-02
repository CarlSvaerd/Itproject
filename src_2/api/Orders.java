// POST /orders

package api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Orders
{
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
        int status = 200;
        
        writeResponse(exchange, status, orderJson);
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
