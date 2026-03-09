import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

public class BookRepository {

    private final List<Book> books = new ArrayList<>();
    private final List<BorrowRecord> borrowHistory = new ArrayList<>();
    private int nextId = 1;

    public BookRepository() {
        books.add(new Book(nextId++, "Clean Code", "Robert C. Martin", "Technology"));
        books.add(new Book(nextId++, "The Pragmatic Programmer", "Andrew Hunt", "Technology"));
        books.add(new Book(nextId++, "Design Patterns", "Gang of Four", "Technology"));
        books.add(new Book(nextId++, "Effective Java", "Joshua Bloch", "Technology"));
        books.add(new Book(nextId++, "The Alchemist", "Paulo Coelho", "Fiction"));
        books.add(new Book(nextId++, "Atomic Habits", "James Clear", "Self-Help"));
    }

    public List<Book> getAll() {
        return Collections.unmodifiableList(books);
    }

    public Optional<Book> getById(int id) {
        return books.stream().filter(b -> b.getId() == id).findFirst();
    }

    public Book add(String title, String author, String genre) {
        Book book = new Book(nextId++, title, author, genre);
        books.add(book);
        return book;
    }

    public boolean delete(int id) {
        return books.removeIf(b -> b.getId() == id);
    }

    public List<Book> searchBooks(String genre, String author, Boolean available) {
        return books.stream()
            .filter(b -> genre == null || b.getGenre().equalsIgnoreCase(genre))
            .filter(b -> author == null || b.getAuthor().toLowerCase().contains(author.toLowerCase()))
            .filter(b -> available == null || b.isAvailable() == available)
            .collect(Collectors.toList());
    }

    public List<Book> paginate(List<Book> list, int page, int size) {
        int from = Math.min((page - 1) * size, list.size());
        int to = Math.min(from + size, list.size());
        return list.subList(from, to);
    }

    public boolean checkout(int id, String borrowerName) {
        Optional<Book> book = getById(id);
        if (book.isPresent() && book.get().isAvailable()) {
            book.get().setAvailable(false);
            borrowHistory.add(new BorrowRecord(
                id, book.get().getTitle(), borrowerName, LocalDateTime.now().toString()
            ));
            return true;
        }
        return false;
    }

    public boolean returnBook(int id) {
        Optional<Book> book = getById(id);
        if (book.isPresent() && !book.get().isAvailable()) {
            book.get().setAvailable(true);
            borrowHistory.stream()
                .filter(r -> r.getBookId() == id && r.getReturnedAt() == null)
                .findFirst()
                .ifPresent(r -> r.setReturnedAt(LocalDateTime.now().toString()));
            return true;
        }
        return false;
    }

    public List<BorrowRecord> getAllHistory() {
        return Collections.unmodifiableList(borrowHistory);
    }

    public List<BorrowRecord> getHistoryByBookId(int id) {
        return borrowHistory.stream()
            .filter(r -> r.getBookId() == id)
            .collect(Collectors.toList());
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    public String getStats() {
        long available = books.stream().filter(Book::isAvailable).count();
        long checkedOut = books.size() - available;
        List<String> genres = books.stream()
            .map(Book::getGenre)
            .distinct()
            .collect(Collectors.toList());
        String genreArray = "[" + genres.stream()
            .map(g -> "\"" + escapeJson(g) + "\"")
            .collect(Collectors.joining(",")) + "]";
        return String.format(
            "{\"totalBooks\":%d,\"availableBooks\":%d,\"checkedOutBooks\":%d,\"totalBorrowRecords\":%d,\"genres\":%s}",
            books.size(), available, checkedOut, borrowHistory.size(), genreArray
        );
    }
}
