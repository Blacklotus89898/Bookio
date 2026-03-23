package Bookio;
import java.sql.*;

public class Main {

    public static void main(String[] args) {
        Connection con  = null;
        Statement  stmt = null;

        try {
            con  = DBConnection.connect();
            stmt = con.createStatement();

            // ── Queries ──────────────────────────────────────────────────────
            Queries.queryAllBooks(stmt);
            Queries.queryCustomerSpending(stmt);
            Queries.queryBookRatings(stmt);
            Queries.queryLowStock(stmt);
            Queries.queryOrderSummary(stmt);

            // ── Modifications ────────────────────────────────────────────────
            Modifications.updateBookPrice(stmt, "1111111111111", 17.99);  // update 1984's price
            Modifications.restockBook(stmt, "7777777777777", 10);          // restock IT
            Modifications.cancelOrder(stmt, 6);                            // cancel order #6
            Modifications.addReview(stmt, 11, "A masterpiece", 5,
            "2026-03-01", "alice@email.com",
            "1000000000096");                      // review for Dune
            Modifications.deleteReview(stmt, 7);                           // delete existing review #7 (bob/IT)
            
        } catch (SQLException e) {
            System.out.println("SQL Error — Code: " + e.getErrorCode()
                             + "  State: " + e.getSQLState());
            System.out.println(e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
            try { if (con  != null) con.close();  } catch (SQLException ignored) {}
            System.out.println("Connection closed.");
        }
    }
}