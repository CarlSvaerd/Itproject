// Starts a HTTP-server

package api;

import com.sun.net.httpserver.HttpServer;
import engine.MatchingEngine;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Exchange 
{
    public HttpServer start() throws IOException 
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        MatchingEngine matchingEngine = new MatchingEngine();
        
        server.createContext("/orders", new Orders(matchingEngine)::handlePost);
        server.createContext("/trades", new Trades()::handleGet);
        
        server.start();
        
        System.out.println("Listening on http://localhost:8080");
        return server;
    }
}