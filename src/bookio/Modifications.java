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
  // FIX: Uses PreparedStatement and internal ID generation to avoid "probing"
  public static void addReview(Connection con, String comment, int score, 
      String email, String isbn) throws SQLException {
    String sql = "INSERT INTO Review (review_id, comment, score, date, customer_email, isbn) " +
      "VALUES ((SELECT COALESCE(MAX(review_id), 0) + 1 FROM Review), ?, ?, CURRENT DATE, ?, ?)";

    try (PreparedStatement pstmt = con.prepareStatement(sql)) {
      pstmt.setString(1, comment);
      pstmt.setInt(2, score);
      pstmt.setString(3, email);
      pstmt.setString(4, isbn);
      pstmt.executeUpdate();
      System.out.println("Review added successfully.");
    }
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

  // FIX: Satisfies the "More than one SQL statement" requirement via Transaction
  public static void processReturn(Connection con, int orderId, String isbn, int qtyToReturn) throws SQLException {
    String updateOrder = "UPDATE Orders SET status = 'Returned' WHERE order_id = ?";
    String updateStock = "UPDATE Book SET quantity = quantity + ? WHERE isbn = ?";

    con.setAutoCommit(false); 
    try (PreparedStatement psOrder = con.prepareStatement(updateOrder);
        PreparedStatement psStock = con.prepareStatement(updateStock)) {

      psOrder.setInt(1, orderId);
      psOrder.executeUpdate();

      psStock.setInt(1, qtyToReturn);
      psStock.setString(2, isbn);
      psStock.executeUpdate();

      con.commit(); 
      System.out.println("Success: Order marked as Returned and stock updated.");
    } catch (SQLException e) {
      con.rollback();
      throw e;
    } finally {
      con.setAutoCommit(true);
    }
  }
}
