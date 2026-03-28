package bookio;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class Queries {

    // Q1 — List all books with their author name and price
    public static void queryAllBooks(Statement stmt) throws SQLException {
        System.out.println("=== Q1: All Books with Author & Price ===");
        String sql =
            "SELECT b.isbn, b.title, a.name AS author, b.price " +
            "FROM Book b JOIN Author a ON b.author_id = a.author_id " +
            "ORDER BY a.name, b.title";
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
          System.out.printf("ISBN: %-15s | %-45s | %-22s | $%.2f%n",
              rs.getString("isbn"),
              rs.getString("title"),
              rs.getString("author"),
              rs.getDouble("price"));
        }
        rs.close();
        System.out.println();
    }

    // Q2 — List all customers and their total amount spent (shipped orders only)
    public static void queryCustomerSpending(Statement stmt) throws SQLException {
      System.out.println("=== Q2: Customer Spending (Shipped Orders) ===");
      String sql =
        "SELECT u.name, c.email, SUM(o.order_price) AS total_spent " +
        "FROM Customer c " +
        "JOIN Users u   ON c.email = u.email " +
        "JOIN Orders o  ON c.email = o.customer_email " +
        "WHERE o.status = 'Shipped' " +
        "GROUP BY u.name, c.email " +
        "ORDER BY total_spent DESC";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        System.out.printf("%-15s | %-25s | Total Spent: $%.2f%n",
            rs.getString("name"),
            rs.getString("email"),
            rs.getDouble("total_spent"));
      }
      rs.close();
      System.out.println();
    }

    // Q3 — Show average review score per book (only books that have reviews)
    public static void queryBookRatings(Statement stmt) throws SQLException {
      System.out.println("=== Q3: Average Score per Book ===");
      String sql =
        "SELECT b.title, AVG(r.score) AS avg_score, COUNT(*) AS num_reviews " +
        "FROM Review r JOIN Book b ON r.isbn = b.isbn " +
        "GROUP BY b.isbn, b.title " +
        "ORDER BY avg_score DESC";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        System.out.printf("%-45s | Avg Score: %.2f | Reviews: %d%n",
            rs.getString("title"),
            rs.getDouble("avg_score"),
            rs.getInt("num_reviews"));
      }
      rs.close();
      System.out.println();
    }

    // Q4 — Books low in stock (quantity <= 20)
    public static void queryLowStock(Statement stmt) throws SQLException {
      System.out.println("=== Q4: Low Stock Books (qty <= 20) ===");
      String sql =
        "SELECT b.title, b.quantity, a.name AS author " +
        "FROM Book b JOIN Author a ON b.author_id = a.author_id " +
        "WHERE b.quantity <= 20 " +
        "ORDER BY b.quantity ASC";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        System.out.printf("%-45s | Stock: %3d | Author: %s%n",
            rs.getString("title"),
            rs.getInt("quantity"),
            rs.getString("author"));
      }
      rs.close();
      System.out.println();
    }

    // Q5 — All orders with customer name, status, and payment method
    public static void queryOrderSummary(Statement stmt) throws SQLException {
      System.out.println("=== Q5: Order Summary ===");
      String sql =
        "SELECT o.order_id, u.name, o.date, o.status, " +
        "       o.order_price, p.method " +
        "FROM Orders o " +
        "JOIN Users u    ON o.customer_email = u.email " +
        "JOIN Payment p  ON o.order_id       = p.order_id " +
        "ORDER BY o.date DESC";
      ResultSet rs = stmt.executeQuery(sql);
      while (rs.next()) {
        System.out.printf("Order #%d | %-15s | %s | %-10s | $%.2f | %s%n",
            rs.getInt("order_id"),
            rs.getString("name"),
            rs.getString("date"),
            rs.getString("status"),
            rs.getDouble("order_price"),
            rs.getString("method"));
      }
      rs.close();
      System.out.println();
    }

// FIX: This provides the required dynamic sub-menu logic
    public static List<String> searchBooksByTitle(Connection con, String keyword) throws SQLException {
        List<String> isbns = new ArrayList<>();
        String sql = "SELECT isbn, title, price FROM Book WHERE LOWER(title) LIKE LOWER(?)";

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();

            int count = 1;
            System.out.println("\n--- Search Results ---");
            while (rs.next()) {
                String isbn = rs.getString("isbn");
                System.out.printf("%d. [%s] %s ($%.2f)%n", count++, isbn, rs.getString("title"), rs.getDouble("price"));
                isbns.add(isbn);
            }
            rs.close();
        }
        return isbns;
    }
}
