package bookio;
import java.sql.*;

public class Modifications {

    // M1 — Update the price of a book by ISBN, then restore it
    public static void updateBookPrice(Statement stmt, String isbn, double newPrice) throws SQLException {
        System.out.println("=== M1: Update Book Price ===");

        // Get current price
        ResultSet rs = stmt.executeQuery("SELECT price FROM Book WHERE isbn = '" + isbn + "'");
        if (!rs.next()) { System.out.println("Book not found.\n"); rs.close(); return; }
        double originalPrice = rs.getDouble("price");
        rs.close();

        // Apply change
        stmt.executeUpdate("UPDATE Book SET price = " + newPrice + " WHERE isbn = '" + isbn + "'");
        System.out.println("Price updated: $" + originalPrice + " -> $" + newPrice);

        // Restore original
        stmt.executeUpdate("UPDATE Book SET price = " + originalPrice + " WHERE isbn = '" + isbn + "'");
        System.out.println("Price restored: $" + newPrice + " -> $" + originalPrice + "\n");
    }

    // M2 — Restock a book by ISBN, then restore original quantity
    public static void restockBook(Statement stmt, String isbn, int addQty) throws SQLException {
        System.out.println("=== M2: Restock Book ===");

        // Get current quantity
        ResultSet rs = stmt.executeQuery("SELECT quantity FROM Book WHERE isbn = '" + isbn + "'");
        if (!rs.next()) { System.out.println("Book not found.\n"); rs.close(); return; }
        int originalQty = rs.getInt("quantity");
        rs.close();

        // Apply change
        stmt.executeUpdate("UPDATE Book SET quantity = quantity + " + addQty + " WHERE isbn = '" + isbn + "'");
        System.out.println("Stock updated: " + originalQty + " -> " + (originalQty + addQty));

        // Restore original
        stmt.executeUpdate("UPDATE Book SET quantity = " + originalQty + " WHERE isbn = '" + isbn + "'");
        System.out.println("Stock restored: " + (originalQty + addQty) + " -> " + originalQty + "\n");
    }

    // M3 — Cancel an order, then restore original status
    public static void cancelOrder(Statement stmt, int orderId) throws SQLException {
        System.out.println("=== M3: Cancel Order ===");

        // Get current status
        ResultSet rs = stmt.executeQuery("SELECT status FROM Orders WHERE order_id = " + orderId);
        if (!rs.next()) { System.out.println("Order not found.\n"); rs.close(); return; }
        String originalStatus = rs.getString("status");
        rs.close();

        // Apply change
        stmt.executeUpdate("UPDATE Orders SET status = 'Cancelled' WHERE order_id = " + orderId);
        System.out.println("Order #" + orderId + " status updated: " + originalStatus + " -> Cancelled");

        // Restore original
        stmt.executeUpdate("UPDATE Orders SET status = '" + originalStatus + "' WHERE order_id = " + orderId);
        System.out.println("Order #" + orderId + " status restored: Cancelled -> " + originalStatus + "\n");
    }

    // M4 — Insert a review, then delete it
    public static void addReview(Statement stmt, int reviewId, String comment, int score,
                                 String date, String email, String isbn) throws SQLException {
        System.out.println("=== M4: Add Review ===");

        // Insert
        stmt.executeUpdate("INSERT INTO Review VALUES (" +
            reviewId + ", '" + comment + "', " + score + ", '" +
            date + "', '" + email + "', '" + isbn + "')");
        System.out.println("Review #" + reviewId + " inserted.");

        // Restore by deleting
        stmt.executeUpdate("DELETE FROM Review WHERE review_id = " + reviewId);
        System.out.println("Review #" + reviewId + " deleted (restored).\n");
    }

    // M5 — Delete a review, then re-insert it
    public static void deleteReview(Statement stmt, int reviewId) throws SQLException {
        System.out.println("=== M5: Delete Review ===");

        // Save current review
        ResultSet rs = stmt.executeQuery(
            "SELECT comment, score, date, customer_email, isbn FROM Review WHERE review_id = " + reviewId);
        if (!rs.next()) { System.out.println("Review not found.\n"); rs.close(); return; }
        String comment = rs.getString("comment");
        int    score   = rs.getInt("score");
        String date    = rs.getString("date");
        String email   = rs.getString("customer_email");
        String isbn    = rs.getString("isbn");
        rs.close();

        // Delete
        stmt.executeUpdate("DELETE FROM Review WHERE review_id = " + reviewId);
        System.out.println("Review #" + reviewId + " deleted.");

        // Restore
        stmt.executeUpdate("INSERT INTO Review VALUES (" +
            reviewId + ", '" + comment + "', " + score + ", '" +
            date + "', '" + email + "', '" + isbn + "')");
        System.out.println("Review #" + reviewId + " re-inserted (restored).\n");
    }
}