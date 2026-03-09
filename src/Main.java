import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        BookRepository repo = new BookRepository();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/books", new BookHandler(repo));
        server.createContext("/health", new HealthHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("=========================================");
        System.out.println("  Library Management API v2.0 - RUNNING ");
        System.out.println("  Server started on port 8080            ");
        System.out.println("=========================================");
        System.out.println();
        System.out.println("Book Endpoints:");
        System.out.println("  GET    /books                     -> List all books");
        System.out.println("  GET    /books?genre=Technology    -> Filter by genre");
        System.out.println("  GET    /books?author=Martin       -> Filter by author");
        System.out.println("  GET    /books?available=true      -> Filter available books");
        System.out.println("  GET    /books?page=1&size=3       -> Paginated results");
        System.out.println("  GET    /books/{id}                -> Get book by ID");
        System.out.println("  POST   /books                     -> Add new book");
        System.out.println("  DELETE /books/{id}                -> Delete book");
        System.out.println("  PUT    /books/{id}/checkout       -> Checkout book");
        System.out.println("  PUT    /books/{id}/return         -> Return book");
        System.out.println("  GET    /books/stats               -> Library statistics");
        System.out.println("  GET    /books/history             -> Full borrow history");
        System.out.println("  GET    /books/{id}/history        -> Book borrow history");
        System.out.println();
        System.out.println("System Endpoints:");
        System.out.println("  GET    /health                    -> Health check");
        System.out.println();
        System.out.println("Pre-loaded Books:");
        repo.getAll().forEach(b -> System.out.println("  " + b.toJson()));
    }
}
