package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/** Serves only the dashboard's known assets, from the repository root. */
public final class Dashboard implements HttpHandler {
    private static final Map<String, String> ASSETS = Map.of(
            "/", "index.html", "/index.html", "index.html",
            "/app.js", "app.js", "/style.css", "style.css");

    public void handle(HttpExchange request) throws IOException {
        String file = ASSETS.get(request.getRequestURI().getPath());
        if (!"GET".equals(request.getRequestMethod())) {
            request.sendResponseHeaders(405, -1);
            request.close();
            return;
        }
        if (file == null || !Files.isRegularFile(Path.of("web", file))) {
            request.sendResponseHeaders(404, -1);
            request.close();
            return;
        }
        String type = file.endsWith(".js") ? "text/javascript" : file.endsWith(".css") ? "text/css" : "text/html";
        byte[] body = Files.readAllBytes(Path.of("web", file));
        request.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        request.getResponseHeaders().set("Cache-Control", "no-store");
        request.sendResponseHeaders(200, body.length);
        try (var output = request.getResponseBody()) { output.write(body); }
    }
}
