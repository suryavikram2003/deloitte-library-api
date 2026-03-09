import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        BookRepository repo = new BookRepository();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Register specific routes BEFORE the catch-all root route
        server.createContext("/books", new BookHandler(repo));
        server.createContext("/health", new HealthHandler());
        server.createContext("/", new FrontendHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("=========================================");
        System.out.println("  Library Management API v2.0 - RUNNING ");
        System.out.println("  Server started on port 8080            ");
        System.out.println("=========================================");
        System.out.println();
        System.out.println("  Frontend UI  : http://localhost:8080/");
        System.out.println("  Books API    : http://localhost:8080/books");
        System.out.println("  Health Check : http://localhost:8080/health");
        System.out.println("  Statistics   : http://localhost:8080/books/stats");
        System.out.println("  History      : http://localhost:8080/books/history");
        System.out.println();
        System.out.println("Pre-loaded Books:");
        repo.getAll().forEach(b -> System.out.println("  " + b.toJson()));
    }
}
