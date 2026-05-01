package server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class WebServer {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/api", new ApiHandler());
        server.createContext("/", new StaticFileHandler("public"));
        
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server is running at http://localhost:" + port);
    }
}
