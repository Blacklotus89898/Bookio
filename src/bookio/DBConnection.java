package bookio;
import java.sql.*;

public class DBConnection {

    private static final String URL = "jdbc:db2://winter2026-comp421.cs.mcgill.ca:50000/comp421";

    public static Connection connect() throws SQLException {
        try { DriverManager.registerDriver(new com.ibm.db2.jcc.DB2Driver()); }
        catch (Exception e) { System.out.println("Driver not found: " + e.getMessage()); }

        // String user     = System.getenv("SOCSUSER");
        // String password = System.getenv("SOCSPASSWD");
        String user     = "cs421g06";
        String password = "Bookio421";

        if (user == null || password == null) {
            System.err.println("Error: SOCSUSER or SOCSPASSWD environment variable not set.");
            System.exit(1);
        }

        Connection con = DriverManager.getConnection(URL, user, password);
        System.out.println("Connected to COMP421.\n");
        return con;
    }
}