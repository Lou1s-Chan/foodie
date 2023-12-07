package ie.foodie.database;

import java.sql.*;

public class OrderDao {

    public static void connect() {
        Connection connection = null;
        try {
            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + "/database/data.db");
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.

            // Drop the old table if exists and create a new table with the specified schema
            statement.executeUpdate("DROP TABLE IF EXISTS order_details");
            statement.executeUpdate("CREATE TABLE order_details (" +
                    "orderID INTEGER PRIMARY KEY, " +
                    "customerID INTEGER, " +
                    "customerAddress TEXT, " +
                    "customerPhone TEXT, " +
                    "foodID INTEGER, " +
                    "price REAL, " +
                    "totalPrice REAL, " +
                    "quantity INTEGER, " +
                    "restaurantID INTEGER, " +
                    "restaurantPhone TEXT, " +
                    "restaurantAddress TEXT, " +
                    "status TEXT)");

            // Example insert (you can add real data insertion logic here)
            statement.executeUpdate("INSERT INTO order_details (customerID, customerAddress, customerPhone, foodID, price, totalPrice, quantity, restaurantID, restaurantPhone, restaurantAddress, status) VALUES (1, '123 Street', '555-1234', 101, 9.99, 19.98, 2, 201, '555-5678', '456 Avenue', 'Pending')");

            // Query and display the inserted data
            ResultSet rs = statement.executeQuery("SELECT * FROM order_details");
            while (rs.next()) {
                System.out.println("Order ID = " + rs.getInt("orderID"));
                System.out.println("Customer ID = " + rs.getInt("customerID"));
                // Add other fields as needed
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}
