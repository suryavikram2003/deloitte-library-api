import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class BookHandler implements HttpHandler {

    private final BookRepository repo;

    public BookHandler(BookRepository repo) {
        this.repo = repo;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String fullPath = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        String[] parts = fullPath.replaceFirst("^/", "").split("/");

        String response;
        int statusCode = 200;

        try {
            if (method.equals("GET") && parts.length == 1) {
                // GET /books (with optional filters and pagination)
                Map<String, String> params = parseQuery(query);
                String genre = params.get("genre");
                String author = params.get("author");
                Boolean available = params.containsKey("available")
                    ? Boolean.parseBoolean(params.get("available")) : null;
                int page = params.containsKey("page") ? Integer.parseInt(params.get("page")) : 1;
                int size = params.containsKey("size") ? Integer.parseInt(params.get("size")) : 10;

                List<Book> filtered = repo.searchBooks(genre, author, available);
                int totalItems = filtered.size();
                List<Book> paged = repo.paginate(filtered, page, size);
                response = JsonUtil.wrapPaginated(paged, page, size, totalItems);

            } else if (method.equals("GET") && parts.length == 2 && parts[1].equals("stats")) {
                // GET /books/stats
                response = "{\"status\":\"success\",\"data\":" + repo.getStats() + "}";

            } else if (method.equals("GET") && parts.length == 2 && parts[1].equals("history")) {
                // GET /books/history
                response = JsonUtil.wrapHistory(repo.getAllHistory());

            } else if (method.equals("GET") && parts.length == 2) {
                // GET /books/{id}
                int id = Integer.parseInt(parts[1]);
                Optional<Book> book = repo.getById(id);
                if (book.isPresent()) {
                    response = JsonUtil.wrapSingle(book.get().toJson());
                } else {
                    statusCode = 404;
                    response = JsonUtil.errorResponse("Book not found with ID: " + id);
                }

            } else if (method.equals("GET") && parts.length == 3 && parts[2].equals("history")) {
                // GET /books/{id}/history
                int id = Integer.parseInt(parts[1]);
                response = JsonUtil.wrapHistory(repo.getHistoryByBookId(id));

            } else if (method.equals("POST") && parts.length == 1) {
                // POST /books
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseSimpleJson(body);
                String title = fields.get("title");
                String bookAuthor = fields.get("author");
                String genre = fields.get("genre");

                if (title == null || title.isEmpty()) {
                    statusCode = 400;
                    response = JsonUtil.errorResponse("title is required and cannot be empty");
                } else if (bookAuthor == null || bookAuthor.isEmpty()) {
                    statusCode = 400;
                    response = JsonUtil.errorResponse("author is required and cannot be empty");
                } else if (genre == null || genre.isEmpty()) {
                    statusCode = 400;
                    response = JsonUtil.errorResponse("genre is required and cannot be empty");
                } else {
                    Book newBook = repo.add(title, bookAuthor, genre);
                    statusCode = 201;
                    response = JsonUtil.wrapSingle(newBook.toJson());
                }

            } else if (method.equals("DELETE") && parts.length == 2) {
                // DELETE /books/{id}
                int id = Integer.parseInt(parts[1]);
                boolean deleted = repo.delete(id);
                if (deleted) {
                    response = JsonUtil.successResponse("Book with ID " + id + " deleted successfully.");
                } else {
                    statusCode = 404;
                    response = JsonUtil.errorResponse("Book not found with ID: " + id);
                }

            } else if (method.equals("PUT") && parts.length == 3 && parts[2].equals("checkout")) {
                // PUT /books/{id}/checkout
                int id = Integer.parseInt(parts[1]);
                String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Map<String, String> fields = parseSimpleJson(body);
                String borrowerName = fields.getOrDefault("borrowerName", "Anonymous");
                boolean success = repo.checkout(id, borrowerName);
                if (success) {
                    response = JsonUtil.successResponse("Book checked out successfully by " + borrowerName);
                } else {
                    statusCode = 400;
                    response = JsonUtil.errorResponse("Book not available or not found.");
                }

            } else if (method.equals("PUT") && parts.length == 3 && parts[2].equals("return")) {
                // PUT /books/{id}/return
                int id = Integer.parseInt(parts[1]);
                boolean success = repo.returnBook(id);
                if (success) {
                    response = JsonUtil.successResponse("Book returned successfully.");
                } else {
                    statusCode = 400;
                    response = JsonUtil.errorResponse("Book was not checked out or not found.");
                }

            } else {
                statusCode = 404;
                response = JsonUtil.errorResponse("Endpoint not found.");
            }

        } catch (NumberFormatException e) {
            statusCode = 400;
            response = JsonUtil.errorResponse("Invalid ID format.");
        } catch (Exception e) {
            statusCode = 500;
            response = JsonUtil.errorResponse("Internal server error: " + e.getMessage());
        }

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
        Logger.log(method, fullPath, statusCode);
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
        }
        return map;
    }

    private Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isEmpty()) return map;
        // Remove surrounding braces and parse key:"value" pairs preserving spaces in values
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        // Match "key":"value" or "key":value pairs
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\"([^\"]+)\"\\s*:\\s*(?:\"([^\"]*)\"|([^,}]*))").matcher(json);
        while (m.find()) {
            String key = m.group(1).trim();
            String value = m.group(2) != null ? m.group(2) : (m.group(3) != null ? m.group(3).trim() : "");
            map.put(key, value);
        }
        return map;
    }
}
