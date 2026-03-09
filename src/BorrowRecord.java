public class BorrowRecord {
    private int bookId;
    private String bookTitle;
    private String borrowerName;
    private String borrowedAt;
    private String returnedAt;

    public BorrowRecord(int bookId, String bookTitle, String borrowerName, String borrowedAt) {
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.borrowerName = borrowerName;
        this.borrowedAt = borrowedAt;
        this.returnedAt = null;
    }

    public int getBookId() { return bookId; }
    public String getBorrowerName() { return borrowerName; }
    public String getReturnedAt() { return returnedAt; }
    public void setReturnedAt(String returnedAt) { this.returnedAt = returnedAt; }

    public String toJson() {
        String returnedStr = (returnedAt != null) ? "\"" + escapeJson(returnedAt) + "\"" : "null";
        return String.format(
            "{\"bookId\":%d,\"bookTitle\":\"%s\",\"borrowerName\":\"%s\",\"borrowedAt\":\"%s\",\"returnedAt\":%s}",
            bookId, escapeJson(bookTitle), escapeJson(borrowerName), escapeJson(borrowedAt), returnedStr
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
