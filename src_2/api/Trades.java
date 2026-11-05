// The door for GET calls /trades
//
// 1. Check that the method is GET
// 2. Read data/trades.json, or return an empty list if it does not exist yet
// 3. Send back the contents with status 200.
//
// Example:
// curl http://localhost:8080/trades

package api;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Trades
{
    public void handleGet(HttpExchange request) throws IOException
    {
        // Reject anything that is not GET
        boolean isGet = "GET".equals(request.getRequestMethod());
        if (isGet == false)
        {
            writeResponse(request, 405, "Use GET\n");
            return;
        }

        getTrades(request);
    }

    private void getTrades(HttpExchange request) throws IOException
    {
        // History is written by MatchingEngine. Before the first order the file does not exist.
        Path tradesFile = Path.of("data", "trades.json");
        if (!Files.exists(tradesFile)) {
            writeResponse(request, 200, "[]\n");
            return;
        }

        byte[] tradesFileBytes = Files.readAllBytes(tradesFile);
        String tradesJson = new String(tradesFileBytes, StandardCharsets.UTF_8);

        writeResponse(request, 200, tradesJson);
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
