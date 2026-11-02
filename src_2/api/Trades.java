// GET /trades

package api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Trades
{
    public void handleGet(HttpExchange exchange) throws IOException
    {
        boolean isGet = "GET".equals(exchange.getRequestMethod());
        if (isGet == false)
        {
            writeResponse(exchange, 405, "Use GET\n");
            return;
        }

        getTrades(exchange);
    }


    private void getTrades(HttpExchange exchange) throws IOException
    {
        Path tradesFile = Path.of("data", "trades.json");
        byte[] tradesFileBytes = Files.readAllBytes(tradesFile);
        String tradesJson = new String(tradesFileBytes, StandardCharsets.UTF_8);
        int status = 200;

        writeResponse(exchange, status, tradesJson);
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
