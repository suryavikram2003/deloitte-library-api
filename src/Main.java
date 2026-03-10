import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        BookRepository repo = new BookRepository();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/books", new BookHandler(repo));
        server.createContext("/health", new HealthHandler());
        server.createContext("/", new FrontendHandler());

        server.setExecutor(null);
        server.start();

        System.out.println("=========================================");
        System.out.println("  Library Management API v2.0 - RUNNING ");
        System.out.println("  Server started on port " + port + "           ");
        System.out.println("=========================================");
        System.out.println();
        System.out.println("  Frontend UI  : http://localhost:" + port + "/");
        System.out.println("  Books API    : http://localhost:" + port + "/books");
        System.out.println("  Health Check : http://localhost:" + port + "/health");
        System.out.println("  Statistics   : http://localhost:" + port + "/books/stats");
        System.out.println("  History      : http://localhost:" + port + "/books/history");
        System.out.println();
        System.out.println("Pre-loaded Books:");
        repo.getAll().forEach(b -> System.out.println("  " + b.toJson()));
    }
}
