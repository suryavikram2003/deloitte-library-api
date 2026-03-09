import java.util.List;
import java.util.stream.Collectors;

public class JsonUtil {

    public static String toJsonArray(List<Book> books) {
        String items = books.stream()
            .map(Book::toJson)
            .collect(Collectors.joining(","));
        return "[" + items + "]";
    }

    public static String historyToJsonArray(List<BorrowRecord> records) {
        String items = records.stream()
            .map(BorrowRecord::toJson)
            .collect(Collectors.joining(","));
        return "[" + items + "]";
    }

    public static String wrapList(List<Book> books) {
        return String.format("{\"status\":\"success\",\"count\":%d,\"data\":%s}",
            books.size(), toJsonArray(books));
    }

    public static String wrapPaginated(List<Book> paged, int page, int size, int totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / size);
        return String.format(
            "{\"status\":\"success\",\"count\":%d,\"data\":%s,\"pagination\":{\"page\":%d,\"size\":%d,\"totalItems\":%d,\"totalPages\":%d}}",
            paged.size(), toJsonArray(paged), page, size, totalItems, totalPages
        );
    }

    public static String wrapSingle(String dataJson) {
        return String.format("{\"status\":\"success\",\"data\":%s}", dataJson);
    }

    public static String wrapHistory(List<BorrowRecord> records) {
        return String.format("{\"status\":\"success\",\"count\":%d,\"data\":%s}",
            records.size(), historyToJsonArray(records));
    }

    public static String successResponse(String message) {
        return String.format("{\"status\":\"success\",\"message\":\"%s\"}", message);
    }

    public static String errorResponse(String message) {
        return String.format("{\"status\":\"error\",\"message\":\"%s\"}", message);
    }
}
