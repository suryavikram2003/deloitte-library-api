import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class HealthHandler implements HttpHandler {

    private final String startTime = LocalDateTime.now().toString();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = String.format(
            "{\"status\":\"healthy\",\"service\":\"Library Management API\",\"version\":\"2.0.0\",\"timestamp\":\"%s\",\"startedAt\":\"%s\"}",
            LocalDateTime.now().toString(), startTime
        );
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
        Logger.log(exchange.getRequestMethod(), exchange.getRequestURI().getPath(), 200);
    }
}
