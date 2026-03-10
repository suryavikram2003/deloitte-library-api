import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        BookRepository repo = new BookRepository();
        // Bind to 0.0.0.0 so Replit can proxy to the server
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);

        // Register specific routes BEFORE the catch-all root route
        server.createContext("/books", new BookHandler(repo));
        server.createContext("/health", new HealthHandler());
        server.createContext("/", new FrontendHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("=========================================");
        System.out.println("  Library Management API v2.0 - RUNNING ");
        System.out.println("  Server started on port " + port);
        System.out.println("=========================================");
        System.out.println();
        System.out.println("  Frontend UI  : http://0.0.0.0:" + port + "/");
        System.out.println("  Books API    : http://0.0.0.0:" + port + "/books");
        System.out.println("  Health Check : http://0.0.0.0:" + port + "/health");
        System.out.println("  Statistics   : http://0.0.0.0:" + port + "/books/stats");
        System.out.println("  History      : http://0.0.0.0:" + port + "/books/history");
        System.out.println();
        System.out.println("Pre-loaded Books:");
        repo.getAll().forEach(b -> System.out.println("  " + b.toJson()));
    }
}