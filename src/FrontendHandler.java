import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FrontendHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] bytes;
        int status = 200;

        try {
            // Try multiple possible locations for web/index.html
            // 1. Relative to working directory (local dev / Docker with WORKDIR=/app)
            // 2. One level up from working directory (when running from out/ subdirectory)
            // 3. Absolute /app/web path (Railway/Docker fallback)
            String[] candidatePaths = {
                System.getProperty("user.dir") + "/web/index.html",
                System.getProperty("user.dir") + "/../web/index.html",
                "/app/web/index.html"
            };

            java.io.File file = null;
            for (String path : candidatePaths) {
                java.io.File candidate = new java.io.File(path);
                if (candidate.exists()) {
                    file = candidate;
                    break;
                }
            }

            if (file != null) {
                bytes = Files.readAllBytes(file.toPath());
            } else {
                String fallback = "<html><body style='font-family:sans-serif;padding:40px;background:#0f172a;color:#e2e8f0'>"  
                    + "<h2 style='color:#22d3ee'>Library Management API v2.0</h2>"  
                    + "<p>API is running. Frontend file not found at web/index.html</p>"  
                    + "<ul><li><a href='/books' style='color:#22d3ee'>GET /books</a></li>"  
                    + "<li><a href='/health' style='color:#22d3ee'>GET /health</a></li>"  
                    + "<li><a href='/books/stats' style='color:#22d3ee'>GET /books/stats</a></li></ul>"  
                    + "</body></html>";
                bytes = fallback.getBytes(StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            bytes = ("<html><body>Error: " + e.getMessage() + "</body></html>").getBytes(StandardCharsets.UTF_8);
            status = 500;
        }

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
        Logger.log(exchange.getRequestMethod(), exchange.getRequestURI().getPath(), status);
    }
}