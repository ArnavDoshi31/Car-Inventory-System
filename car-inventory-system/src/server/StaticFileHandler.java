package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class StaticFileHandler implements HttpHandler {
    private final String baseDir;

    public StaticFileHandler(String baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        File file = new File(baseDir + path);
        if (!file.exists() || file.isDirectory()) {
            String response = "404 Not Found";
            exchange.sendResponseHeaders(404, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }

        String contentType = Files.probeContentType(file.toPath());
        if (contentType != null) {
            exchange.getResponseHeaders().set("Content-Type", contentType);
        }

        exchange.sendResponseHeaders(200, file.length());
        try (OutputStream os = exchange.getResponseBody();
             FileInputStream fs = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int count;
            while ((count = fs.read(buffer)) != -1) {
                os.write(buffer, 0, count);
            }
        }
    }
}
