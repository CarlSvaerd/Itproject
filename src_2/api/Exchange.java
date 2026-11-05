// 1. Creates a HTTP-server on port 8080
// 2. Makes a MatchingEngine object.
// 3. Copies two URLs to two methods, handlePost and handleGet.
// 4. Starts the server.

// When someone calls http://localhost:8080/orders the Orders is called and the same for trades.

package api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import engine.MatchingEngine;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Exchange
{
    public HttpServer start() throws IOException
    {
        // Create the server and one shared matching engine for all requests
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        MatchingEngine matchingEngine = new MatchingEngine();

        // Handler for placing orders
        Orders ordersHandler = new Orders(matchingEngine);
        HttpHandler ordersPostHandler = ordersHandler::handlePost;

        // Handler for reading trade history
        Trades tradesHandler = new Trades();
        HttpHandler tradesGetHandler = tradesHandler::handleGet;

        server.createContext("/orders", ordersPostHandler);
        server.createContext("/trades", tradesGetHandler);

        server.start();
        
        System.out.println("Listening on http://localhost:8080");
        return server;
    }
}
