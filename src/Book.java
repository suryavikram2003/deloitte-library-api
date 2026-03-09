public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private boolean available;

    public Book(int id, String title, String author, String genre) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.available = true;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String toJson() {
        return String.format(
            "{\"id\":%d,\"title\":\"%s\",\"author\":\"%s\",\"genre\":\"%s\",\"available\":%b}",
            id, escapeJson(title), escapeJson(author), escapeJson(genre), available
        );
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
