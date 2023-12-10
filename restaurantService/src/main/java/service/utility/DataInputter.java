package service.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DataInputter {
    public static void main(String[] args) {
        System.out.println("Running SQL items adder");
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        // Load the SQLite JDBC driver (Not required with newer JDBC versions)
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // Database URL
        String url = "jdbc:sqlite:restaurantService/database/restaurantdatabase.db";

        // Establishing the connection
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                // Creating a table
                createMenuItemTable(conn);

                // Inserting data
                insertSampleMenuItems(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createMenuItemTable(Connection conn) throws SQLException {
        String sqlCreateTable = "CREATE TABLE IF NOT EXISTS menu_item (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "restaurant_id INTEGER," +
                "name TEXT NOT NULL," +
                "description TEXT," +
                "price DECIMAL(10, 2))";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sqlCreateTable);
        }
    }

    private static void insertSampleMenuItems(Connection conn) throws SQLException {
        String[] menuItems = {
                "INSERT INTO menu_item (restaurant_id, name, description, price) VALUES (1, 'Margherita Pizza', 'Classic Margherita with fresh mozzarella and basil', 8.99);" +
                "INSERT INTO menu_item (restaurant_id, name, description, price) VALUES (1, 'Spaghetti Carbonara', 'Creamy pasta with bacon and Parmesan cheese', 10.50);"+
                "INSERT INTO menu_item (restaurant_id, name, description, price) VALUES (1, 'Caesar Salad', 'Crisp romaine lettuce with Caesar dressing and croutons', 7.00);"+
                "INSERT INTO menu_item (restaurant_id, name, description, price) VALUES (1, 'Grilled Salmon', 'Grilled salmon fillet with a lemon butter sauce', 15.00);"+
                "INSERT INTO menu_item (restaurant_id, name, description, price) VALUES (1, 'Tiramisu', 'Traditional Italian dessert with mascarpone and espresso', 6.50);"

        };

        try (Statement stmt = conn.createStatement()) {
            for (int restaurantId = 1; restaurantId <= 30; restaurantId++) {
                for (String menuItem : menuItems) {
                    // Replace restaurant_id in SQL with the current restaurantId
                    String sqlInsert = menuItem.replaceFirst("VALUES \\(1", "VALUES (" + restaurantId);
                    System.out.println(sqlInsert);
                    stmt.executeUpdate(sqlInsert);
                }
            }
        }
    }
}
