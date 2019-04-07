package utils;

import java.sql.*;

public class SqliteHelper {
    private static Connection c = null;

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if (c == null) {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:/home/rookurin/Databases/baike.db");
            try (Statement stmt = c.createStatement()) {
                stmt.execute("pragma busy_timeout=3000");
            }
        }
        return c;
    }
}
