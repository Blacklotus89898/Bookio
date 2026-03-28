package bookio;

import java.sql.*;
import java.util.Scanner;
import java.util.List; // FIX: Added missing import
import java.util.ArrayList; // FIX: Added missing import

public class MainCLI {

  public static void main(String[] args) {
    Connection con = null;
    Statement stmt = null;

    try {
      con = DBConnection.connect();
      stmt = con.createStatement();
      Scanner scanner = new Scanner(System.in);

      boolean running = true;
      while (running) {
        printMenu();
        String input = scanner.nextLine().trim();

        switch (input) {
          case "1": Queries.queryAllBooks(stmt); break;
          case "2": Queries.queryCustomerSpending(stmt); break;
          case "3": Queries.queryBookRatings(stmt); break;
          case "4": Queries.queryLowStock(stmt); break;
          case "5": Queries.queryOrderSummary(stmt); break;
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
          case "9": // FIX: Sub-menu Requirement
                    System.out.print("Search for book title to review: ");
                    String keyword = scanner.nextLine().trim();
                    List<String> foundIsbns = Queries.searchBooksByTitle(con, keyword);
                    if (foundIsbns.isEmpty()) {
                      System.out.println("No books found.");
                    } else {
                      System.out.print("Select number to review (or 0 to cancel): ");
                      int choice = Integer.parseInt(scanner.nextLine().trim());
                      if (choice > 0 && choice <= foundIsbns.size()) {
                        String selectedIsbn = foundIsbns.get(choice - 1);
                        System.out.print("Score (1-5): ");
                        int score = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Comment: ");
                        String comment = scanner.nextLine().trim();
                        System.out.print("Customer email: ");
                        String email = scanner.nextLine().trim();
                        Modifications.addReview(con, comment, score, email, selectedIsbn);
                      }
                    }
                    break;
          case "10":
                    System.out.print("Enter Review ID to delete: ");
                    int delId = Integer.parseInt(scanner.nextLine().trim());
                    Modifications.deleteReview(stmt, delId);
                    break;
          case "11": // FIX: Multiple SQL Statement Requirement
                    System.out.print("Enter Order ID to return: ");
                    int retOrderId = Integer.parseInt(scanner.nextLine().trim());
                    System.out.print("Enter ISBN: ");
                    String retIsbn = scanner.nextLine().trim();
                    System.out.print("Enter quantity: ");
                    int retQty = Integer.parseInt(scanner.nextLine().trim());
                    Modifications.processReturn(con, retOrderId, retIsbn, retQty);
                    break;
          case "0":
                    running = false;
                    System.out.println("Goodbye!");
                    break;
          default:
                    System.out.println("Invalid option.\n");
        }
      }
      scanner.close();
    } catch (SQLException e) {
      System.out.println("SQL Error: " + e.getMessage());
    } finally {
      try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
      try { if (con != null) con.close(); } catch (SQLException ignored) {}
      System.out.println("Connection closed.");
    }
  }

  static void printMenu() {
    System.out.println("\n╔══════════════════════════════╗");
    System.out.println("║       Bookio DB Manager      ║");
    System.out.println("╠══════════════════════════════╣");
    System.out.println("║ 1. All books                 ║");
    System.out.println("║ 2. Customer spending         ║");
    System.out.println("║ 3. Book ratings              ║");
    System.out.println("║ 4. Low stock books           ║");
    System.out.println("║ 5. Order summary             ║");
    System.out.println("╠══════════════════════════════╣");
    System.out.println("║ 6. Update book price         ║");
    System.out.println("║ 7. Restock book              ║");
    System.out.println("║ 8. Cancel order              ║");
    System.out.println("║ 9. Add review (Sub-Menu)     ║");
    System.out.println("║ 10. Delete review            ║");
    System.out.println("║ 11. Process Return (Complex) ║"); // FIX: Added to menu
    System.out.println("╠══════════════════════════════╣");
    System.out.println("║ 0. Exit                      ║");
    System.out.println("╚══════════════════════════════╝");
    System.out.print("Choose: ");
  }
}
