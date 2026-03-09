import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String method, String path, int statusCode) {
        String statusText = getStatusText(statusCode);
        String timestamp = LocalDateTime.now().format(fmt);
        System.out.printf("[%s] %s %s -> %d %s%n", timestamp, method, path, statusCode, statusText);
    }

    private static String getStatusText(int code) {
        switch (code) {
            case 200: return "OK";
            case 201: return "Created";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }
}
