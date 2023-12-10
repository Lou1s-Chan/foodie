package service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

// create/edit user db
public class ConnectDB {
    public static void main(String[] args) {
        System.out.println("Running SQL items adder");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));
        // Database URL
        String url = "jdbc:sqlite:userService/database/userdatabase.db";

        // Establishing the connection
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                // Creating a table
                createUserTable(conn);

                // Inserting data
                insertUserTable(conn);
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createUserTable(Connection conn) throws SQLException {
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS user (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT NOT NULL," +
                "password TEXT NOT NULL," +
                "phone INTEGER," +
                "address TEXT)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCreateTable);
        }
    }

    private static void insertUserTable(Connection conn) throws SQLException {
        String[] userInfos = {
                "INSERT INTO user ( username,password, phone, address) VALUES ('Moo','password1', 123456789, 'Dublin 1');"

        };

        try (Statement stmt = conn.createStatement()) {
            for (String userInfo : userInfos) {
                stmt.execute(userInfo);
            }
        }
    }
}
