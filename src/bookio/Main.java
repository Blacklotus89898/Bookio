package bookio;
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
            Modifications.updateBookPrice(stmt, "1111111111111", 17.99);
            Modifications.restockBook(stmt, "7777777777777", 10);
            Modifications.cancelOrder(stmt, 6);
            
            // FIX: Changed 'stmt' to 'con' and removed manual ID and Date
            Modifications.addReview(con, "A masterpiece", 5, 
                                   "alice@email.com", "1000000000096");
            
            Modifications.deleteReview(stmt, 7);
            
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
