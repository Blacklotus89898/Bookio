package bookio;
/*
# FIXME: add a 2 query optionsfor complex action, 1 taskk requires sub-menu based on db query
at least 5 options
*/
import java.sql.*;
import java.util.Scanner;

public class MainCLI {

    public static void main(String[] args) {
        Connection con  = null;
        Statement  stmt = null;

        try {
            con  = DBConnection.connect();
            stmt = con.createStatement();
            Scanner scanner = new Scanner(System.in);

            boolean running = true;
            while (running) {
                printMenu();
                String input = scanner.nextLine().trim();

                switch (input) {
                    // ── Queries ──────────────────────────────────────────────
                    case "1": Queries.queryAllBooks(stmt);        break;
                    case "2": Queries.queryCustomerSpending(stmt); break;
                    case "3": Queries.queryBookRatings(stmt);     break;
                    case "4": Queries.queryLowStock(stmt);        break;
                    case "5": Queries.queryOrderSummary(stmt);    break;

                    // ── Modifications ─────────────────────────────────────────
                    case "6":
                        System.out.print("Enter ISBN: ");
                        String isbn6 = scanner.nextLine().trim();
                        System.out.print("Enter new price: ");
                        double price = Double.parseDouble(scanner.nextLine().trim());
                        Modifications.updateBookPrice(stmt, isbn6, price);
                        break;

                    case "7":
                        System.out.print("Enter ISBN: ");
                        String isbn7 = scanner.nextLine().trim();
                        System.out.print("Enter quantity to add: ");
                        int qty = Integer.parseInt(scanner.nextLine().trim());
                        Modifications.restockBook(stmt, isbn7, qty);
                        break;

                    case "8":
                        System.out.print("Enter Order ID: ");
                        int orderId = Integer.parseInt(scanner.nextLine().trim());
                        Modifications.cancelOrder(stmt, orderId);
                        break;

                    case "9":
                        System.out.print("Enter Review ID: ");
                        int reviewId = Integer.parseInt(scanner.nextLine().trim());
                        Queries.queryAllBooks(stmt); // show books to help user choose ISBN
                        System.out.print("Enter ISBN: ");
                        String isbn9 = scanner.nextLine().trim();
                        System.out.print("Enter comment: ");
                        String comment = scanner.nextLine().trim();
                        System.out.print("Enter score (1-5): ");
                        int score = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Enter date (YYYY-MM-DD): ");
                        String date = scanner.nextLine().trim();
                        System.out.print("Enter customer email: ");
                        String email = scanner.nextLine().trim();
                        Modifications.addReview(stmt, reviewId, comment, score, date, email, isbn9);
                        break;

                    case "10":
                        System.out.print("Enter Review ID to delete: ");
                        int delId = Integer.parseInt(scanner.nextLine().trim());
                        Modifications.deleteReview(stmt, delId);
                        break;

                    case "0":
                        running = false;
                        System.out.println("Goodbye!");
                        break;

                    default:
                        System.out.println("Invalid option, try again.\n");
                }
            }
            scanner.close();

        } catch (SQLException e) { // Error handling for all SQL operations
            System.out.println("SQL Error — Code: " + e.getErrorCode()
                             + "  State: " + e.getSQLState());
            System.out.println(e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
            try { if (con  != null) con.close();  } catch (SQLException ignored) {}
            System.out.println("Connection closed.");
        }
    }

    static void printMenu() {
        System.out.println("╔══════════════════════════════╗");
        System.out.println("║       Bookio DB Manager      ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  QUERIES                     ║");
        System.out.println("║  1. All books                ║");
        System.out.println("║  2. Customer spending        ║");
        System.out.println("║  3. Book ratings             ║");
        System.out.println("║  4. Low stock books          ║");
        System.out.println("║  5. Order summary            ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  MODIFICATIONS               ║");
        System.out.println("║  6. Update book price        ║");
        System.out.println("║  7. Restock book             ║");
        System.out.println("║  8. Cancel order             ║");
        System.out.println("║  9. Add review               ║");
        System.out.println("║  10. Delete review           ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║  0. Exit                     ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choose: ");
    }
} 
