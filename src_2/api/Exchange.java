// Starts a HTTP-server

package api;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Exchange 
{
    public void start() throws IOException 
    {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/orders", new Orders());
        server.createContext("/trades", new Trades());
        
        server.start();
        
        System.out.println("Listening on http://localhost:8080");
    }
}