// GET /trades

package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Trades implements HttpHandler
{
    public void handle(HttpExchange http) throws IOException
    {
        if (!"GET".equals(http.getRequestMethod()))
        {
            send(http, 405, "Use GET\n");
            return;
        }

        String body = Files.readString(Path.of("data", "trades.json"));
        send(http, 200, body);
    }

    private static void send(HttpExchange http, int status, String body) throws IOException
    {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        
        http.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        http.sendResponseHeaders(status, bytes.length);
        
        try (OutputStream out = http.getResponseBody())
        {
            out.write(bytes);
        }
    }
}
