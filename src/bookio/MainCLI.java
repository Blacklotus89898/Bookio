package bookio;

import java.sql.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class MainCLI {

    public static void main(String[] args) {
        Connection con = null;
        Statement stmt = null;
        Scanner scanner = new Scanner(System.in);

        try {
            con = DBConnection.connect();
            stmt = con.createStatement();

            boolean running = true;
            while (running) {
                printMenu();
                String input = scanner.nextLine().trim();

                // NESTED TRY-CATCH: This keeps the loop running even if an error occurs
                try {
                    switch (input) {
                        case "1": Queries.queryAllBooks(stmt); break;
                        case "2": Queries.queryCustomerSpending(stmt); break;
                        case "3": Queries.queryBookRatings(stmt); break;
                        case "4": Queries.queryLowStock(stmt); break;
                        case "5": Queries.queryOrderSummary(stmt); break;
                        
                        case "6":
                            System.out.print("Enter ISBN: ");
                            String isbn6 = scanner.nextLine().trim();
                            double price = getDoubleInput(scanner, "Enter new price: ");
                            Modifications.updateBookPrice(stmt, isbn6, price);
                            break;

                        case "7":
                            System.out.print("Enter ISBN: ");
                            String isbn7 = scanner.nextLine().trim();
                            int qty = getIntInput(scanner, "Enter quantity to add: ");
                            Modifications.restockBook(stmt, isbn7, qty);
                            break;

                        case "8":
                            int orderId = getIntInput(scanner, "Enter Order ID: ");
                            Modifications.cancelOrder(stmt, orderId);
                            break;

                        case "9": // Sub-menu logic
                            System.out.print("Search for book title to review: ");
                            String keyword = scanner.nextLine().trim();
                            List<String> foundIsbns = Queries.searchBooksByTitle(con, keyword);
                            
                            if (foundIsbns.isEmpty()) {
                                System.out.println("No books found.");
                            } else {
                                int choice = getIntInput(scanner, "Select number to review (or 0 to cancel): ");
                                if (choice > 0 && choice <= foundIsbns.size()) {
                                    String selectedIsbn = foundIsbns.get(choice - 1);
                                    int score = getIntInput(scanner, "Score (1-5): ");
                                    System.out.print("Comment: ");
                                    String comment = scanner.nextLine().trim();
                                    System.out.print("Customer email: ");
                                    String email = scanner.nextLine().trim();
                                    Modifications.addReview(con, comment, score, email, selectedIsbn);
                                }
                            }
                            break;

                        case "10":
                            int delId = getIntInput(scanner, "Enter Review ID to delete: ");
                            Modifications.deleteReview(stmt, delId);
                            break;

                        case "11": // Multiple SQL statements
                            int retOrderId = getIntInput(scanner, "Enter Order ID to return: ");
                            System.out.print("Enter ISBN: ");
                            String retIsbn = scanner.nextLine().trim();
                            int retQty = getIntInput(scanner, "Enter quantity: ");
                            Modifications.processReturn(con, retOrderId, retIsbn, retQty);
                            break;

                        case "0":
                            running = false;
                            System.out.println("Goodbye!");
                            break;

                        default:
                            System.out.println("Invalid option. Please try again.");
                    }
                } catch (SQLException e) {
                    System.out.println("\n--- DATABASE ERROR ---");
                    System.out.println("Message: " + e.getMessage());
                    System.out.println("The program will return to the main menu.");
                } catch (Exception e) {
                    System.out.println("\n--- GENERAL ERROR ---");
                    System.out.println("An unexpected error occurred: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Critical Connection Error: " + e.getMessage());
        } finally {
            // Cleanup: Ensures resources are closed regardless of errors
            try { if (stmt != null) stmt.close(); } catch (SQLException ignored) {}
            try { if (con != null) con.close(); } catch (SQLException ignored) {}
            scanner.close();
            System.out.println("Connection closed.");
        }
    }

    // HELPER: Handles non-integer input gracefully
    private static int getIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number.");
            }
        }
    }

    // HELPER: Handles non-double input gracefully
    private static double getDoubleInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid price (e.g., 19.99).");
            }
        }
    }

    static void printMenu() {
        System.out.println("\n╔══════════════════════════════╗");
        System.out.println("║        Bookio DB Manager     ║");
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
        System.out.println("║ 11. Process Return (Complex) ║");
        System.out.println("╠══════════════════════════════╣");
        System.out.println("║ 0. Exit                      ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.print("Choose: ");
    }
}
