import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FrontendHandler implements HttpHandler {

    // The web directory is always at /app/web inside the Docker container
    // because Dockerfile does: WORKDIR /app and COPY web/ ./web/
    private static final String WEB_DIR = "/app/web";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        byte[] bytes;
        int status = 200;

        try {
            java.io.File file = new java.io.File(WEB_DIR + "/index.html");

            // Fallback: also try relative to user.dir (for local/Replit dev)
            if (!file.exists()) {
                file = new java.io.File(System.getProperty("user.dir") + "/web/index.html");
            }

            if (file.exists()) {
                bytes = Files.readAllBytes(file.toPath());
            } else {
                String fallback = "<html><body style='font-family:sans-serif;padding:40px;background:#0f172a;color:#e2e8f0'>"
                    + "<h2 style='color:#22d3ee'>Library Management API v2.0</h2>"
                    + "<p>API is running. Frontend file not found at " + file.getAbsolutePath() + "</p>"
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