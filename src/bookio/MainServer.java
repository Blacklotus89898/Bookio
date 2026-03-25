package bookio;


import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;

/**
 * Minimal HTTP server using only the JDK built-in HttpServer.
 * No Spring, no extra jars needed.
 *
 * Endpoints:
 *   GET  /api/books              -> Q1 all books
 *   GET  /api/spending           -> Q2 customer spending
 *   GET  /api/ratings            -> Q3 book ratings
 *   GET  /api/lowstock           -> Q4 low stock
 *   GET  /api/orders             -> Q5 order summary
 *   POST /api/price?isbn=X&price=Y    -> M1 update price
 *   POST /api/restock?isbn=X&qty=Y    -> M2 restock
 *   POST /api/cancel?orderid=X        -> M3 cancel order
 *   POST /api/review/add?id=X&comment=X&score=X&date=X&email=X&isbn=X -> M4
 *   POST /api/review/delete?id=X      -> M5 delete review
 */
public class MainServer {

    static Connection con;

    public static void main(String[] args) throws Exception {
        con = DBConnection.connect();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ── Queries ──────────────────────────────────────────────────────────
        server.createContext("/api/books",    h -> handle(h, MainServer::booksJson));
        server.createContext("/api/spending", h -> handle(h, MainServer::spendingJson));
        server.createContext("/api/ratings",  h -> handle(h, MainServer::ratingsJson));
        server.createContext("/api/lowstock", h -> handle(h, MainServer::lowStockJson));
        server.createContext("/api/orders",   h -> handle(h, MainServer::ordersJson));

        // ── Modifications ─────────────────────────────────────────────────────
        server.createContext("/api/price",          h -> handlePost(h, MainServer::updatePrice));
        server.createContext("/api/restock",         h -> handlePost(h, MainServer::restock));
        server.createContext("/api/cancel",          h -> handlePost(h, MainServer::cancelOrder));
        server.createContext("/api/review/add",      h -> handlePost(h, MainServer::addReview));
        server.createContext("/api/review/delete",   h -> handlePost(h, MainServer::deleteReview));

        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://localhost:8080");
        System.out.println("Press Ctrl+C to stop.");
    }

    // ── Handler wiring ────────────────────────────────────────────────────────

    @FunctionalInterface interface JsonProducer { String produce() throws Exception; }

    static void handlePost(HttpExchange h, JsonProducer producer) throws IOException {
        if (!h.getRequestMethod().equalsIgnoreCase("POST")) {
            String msg = "{\"error\":\"POST required\"}";
            h.sendResponseHeaders(405, msg.length());
            h.getResponseBody().write(msg.getBytes());
            h.getResponseBody().close();
            return;
        }
        handle(h, producer);
    }

    // ── Query handlers ────────────────────────────────────────────────────────

    static String booksJson() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT b.isbn, b.title, a.name AS author, b.price " +
            "FROM Book b JOIN Author a ON b.author_id = a.author_id " +
            "ORDER BY a.name, b.title");
        StringBuilder sb = new StringBuilder("[");
        while (rs.next()) {
            if (sb.length() > 1) sb.append(",");
            sb.append(String.format(
                "{\"isbn\":\"%s\",\"title\":\"%s\",\"author\":\"%s\",\"price\":%.2f}",
                esc(rs.getString("isbn")), esc(rs.getString("title")),
                esc(rs.getString("author")), rs.getDouble("price")));
        }
        rs.close(); stmt.close();
        return sb.append("]").toString();
    }

    static String spendingJson() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT u.name, c.email, SUM(o.order_price) AS total_spent " +
            "FROM Customer c " +
            "JOIN Users u  ON c.email = u.email " +
            "JOIN Orders o ON c.email = o.customer_email " +
            "WHERE o.status = 'Shipped' " +
            "GROUP BY u.name, c.email ORDER BY total_spent DESC");
        StringBuilder sb = new StringBuilder("[");
        while (rs.next()) {
            if (sb.length() > 1) sb.append(",");
            sb.append(String.format(
                "{\"name\":\"%s\",\"email\":\"%s\",\"total_spent\":%.2f}",
                esc(rs.getString("name")), esc(rs.getString("email")),
                rs.getDouble("total_spent")));
        }
        rs.close(); stmt.close();
        return sb.append("]").toString();
    }

    static String ratingsJson() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT b.title, AVG(r.score) AS avg_score, COUNT(*) AS num_reviews " +
            "FROM Review r JOIN Book b ON r.isbn = b.isbn " +
            "GROUP BY b.isbn, b.title ORDER BY avg_score DESC");
        StringBuilder sb = new StringBuilder("[");
        while (rs.next()) {
            if (sb.length() > 1) sb.append(",");
            sb.append(String.format(
                "{\"title\":\"%s\",\"avg_score\":%.2f,\"num_reviews\":%d}",
                esc(rs.getString("title")), rs.getDouble("avg_score"),
                rs.getInt("num_reviews")));
        }
        rs.close(); stmt.close();
        return sb.append("]").toString();
    }

    static String lowStockJson() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT b.title, b.quantity, a.name AS author " +
            "FROM Book b JOIN Author a ON b.author_id = a.author_id " +
            "WHERE b.quantity <= 20 ORDER BY b.quantity ASC");
        StringBuilder sb = new StringBuilder("[");
        while (rs.next()) {
            if (sb.length() > 1) sb.append(",");
            sb.append(String.format(
                "{\"title\":\"%s\",\"quantity\":%d,\"author\":\"%s\"}",
                esc(rs.getString("title")), rs.getInt("quantity"),
                esc(rs.getString("author"))));
        }
        rs.close(); stmt.close();
        return sb.append("]").toString();
    }

    static String ordersJson() throws Exception {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT o.order_id, u.name, o.date, o.status, o.order_price, p.method " +
            "FROM Orders o " +
            "JOIN Users u   ON o.customer_email = u.email " +
            "JOIN Payment p ON o.order_id = p.order_id " +
            "ORDER BY o.date DESC");
        StringBuilder sb = new StringBuilder("[");
        while (rs.next()) {
            if (sb.length() > 1) sb.append(",");
            sb.append(String.format(
                "{\"order_id\":%d,\"name\":\"%s\",\"date\":\"%s\",\"status\":\"%s\",\"order_price\":%.2f,\"method\":\"%s\"}",
                rs.getInt("order_id"), esc(rs.getString("name")),
                rs.getString("date"), esc(rs.getString("status")),
                rs.getDouble("order_price"), esc(rs.getString("method"))));
        }
        rs.close(); stmt.close();
        return sb.append("]").toString();
    }

    // ── Modification handlers ─────────────────────────────────────────────────

    static String updatePrice() throws Exception {
        String[] q = queryParams();
        String isbn = param(q, "isbn");
        double price = Double.parseDouble(param(q, "price"));
        Statement stmt = con.createStatement();
        int rows = stmt.executeUpdate(
            "UPDATE Book SET price = " + price + " WHERE isbn = '" + isbn + "'");
        stmt.close();
        return rows > 0 ? "{\"status\":\"ok\"}" : "{\"status\":\"not found\"}";
    }

    static String restock() throws Exception {
        String[] q = queryParams();
        String isbn = param(q, "isbn");
        int qty = Integer.parseInt(param(q, "qty"));
        Statement stmt = con.createStatement();
        int rows = stmt.executeUpdate(
            "UPDATE Book SET quantity = quantity + " + qty + " WHERE isbn = '" + isbn + "'");
        stmt.close();
        return rows > 0 ? "{\"status\":\"ok\"}" : "{\"status\":\"not found\"}";
    }

    static String cancelOrder() throws Exception {
        String[] q = queryParams();
        int orderId = Integer.parseInt(param(q, "orderid"));
        Statement stmt = con.createStatement();
        int rows = stmt.executeUpdate(
            "UPDATE Orders SET status = 'Cancelled' WHERE order_id = " + orderId);
        stmt.close();
        return rows > 0 ? "{\"status\":\"ok\"}" : "{\"status\":\"not found\"}";
    }

    static String addReview() throws Exception {
        String[] q = queryParams();
        int id       = Integer.parseInt(param(q, "id"));
        String comment = param(q, "comment");
        int score    = Integer.parseInt(param(q, "score"));
        String date  = param(q, "date");
        String email = param(q, "email");
        String isbn  = param(q, "isbn");
        Statement stmt = con.createStatement();
        stmt.executeUpdate("INSERT INTO Review VALUES (" +
            id + ",'" + esc(comment) + "'," + score + ",'" + date + "','" + email + "','" + isbn + "')");
        stmt.close();
        return "{\"status\":\"ok\"}";
    }

    static String deleteReview() throws Exception {
        String[] q = queryParams();
        int id = Integer.parseInt(param(q, "id"));
        Statement stmt = con.createStatement();
        int rows = stmt.executeUpdate("DELETE FROM Review WHERE review_id = " + id);
        stmt.close();
        return rows > 0 ? "{\"status\":\"ok\"}" : "{\"status\":\"not found\"}";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    // Thread-local exchange for reading query params inside lambda
    static final ThreadLocal<HttpExchange> currentExchange = new ThreadLocal<>();

    static void handle(HttpExchange h, JsonProducer p) throws IOException {
        currentExchange.set(h);
        h.getResponseHeaders().set("Content-Type", "application/json");
        h.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        try {
            String body = p.produce();
            byte[] bytes = body.getBytes();
            h.sendResponseHeaders(200, bytes.length);
            h.getResponseBody().write(bytes);
        } catch (Exception e) {
            String err = "{\"error\":\"" + e.getMessage().replace("\"","'") + "\"}";
            byte[] bytes = err.getBytes();
            h.sendResponseHeaders(500, bytes.length);
            h.getResponseBody().write(bytes);
        }
        h.getResponseBody().close();
        currentExchange.remove();
    }

    static String[] queryParams() {
        HttpExchange h = currentExchange.get();
        String query = h.getRequestURI().getQuery();
        return query != null ? query.split("&") : new String[0];
    }

    static String param(String[] params, String key) {
        for (String p : params)
            if (p.startsWith(key + "="))
                return java.net.URLDecoder.decode(p.substring(key.length() + 1), java.nio.charset.StandardCharsets.UTF_8);
        return "";
    }

    static String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}