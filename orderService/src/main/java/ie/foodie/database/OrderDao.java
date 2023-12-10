package ie.foodie.database;

import ie.foodie.messages.CustomerOrderMessage;
import ie.foodie.messages.OrderConfirmMessage;
import ie.foodie.messages.PaymentConfirmMessage;
import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderDao {

    private Connection connection = null;

    public OrderDao(String dbPath) {
        // create a database connection
        createConnection(dbPath);
        createSchema();
    }

    public OrderDao() {
        // create a database connection
        createConnection(null);
        createSchema();
    }

    private void createConnection(String dbPath) {
        try {
            if (dbPath == null)
                dbPath = "/src/main/resources/data.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + dbPath);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void createSchema() {
        try {
            Statement statement = connection.createStatement();
            statement.setQueryTimeout(30); // set timeout to 30 sec.
            // Drop the old table if exists and create a new table with the specified schema
            statement.executeUpdate("DROP TABLE IF EXISTS orders");
            statement.executeUpdate("CREATE TABLE orders (" +
                    "orderID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "customerID INTEGER NOT NULL, " +
                    "customerAddress TEXT NOT NULL, " +
                    "customerPhone TEXT NOT NULL, " +
                    "totalPrice REAL NOT NULL, " +
                    "status TEXT NOT NULL)");

            // Create the 'restaurants' table
            statement.executeUpdate("DROP TABLE IF EXISTS restaurants");
            statement.executeUpdate("CREATE TABLE restaurants (" +
                    "restaurantID INTEGER PRIMARY KEY, " +
                    "restaurantPhone TEXT NOT NULL, " +
                    "restaurantAddress TEXT NOT NULL)");

            // Create the 'order_restaurant_foods' table
            statement.executeUpdate("DROP TABLE IF EXISTS order_restaurant_foods");
            statement.executeUpdate("CREATE TABLE order_restaurant_foods (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "orderID INTEGER NOT NULL, " +
                    "restaurantID INTEGER NOT NULL, " +
                    "foodID INTEGER NOT NULL, " +
                    "price REAL NOT NULL, " +
                    "quantity INTEGER NOT NULL, " +
                    "FOREIGN KEY(orderID) REFERENCES orders(orderID), " +
                    "FOREIGN KEY(restaurantID) REFERENCES restaurants(restaurantID))");

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public CustomerOrderMessage selectByOrderId(int orderId) {
        try {
            // Fetch order and customer details
            PreparedStatement orderStmt = connection.prepareStatement("SELECT * FROM orders WHERE orderID = ?");
            orderStmt.setInt(1, orderId);
            ResultSet orderRs = orderStmt.executeQuery();

            if (!orderRs.next()) {
                return null; // Order not found
            }

            Customer customer = new Customer(orderRs.getInt("customerID"), orderRs.getString("customerAddress"), orderRs.getString("customerPhone"));

            // Fetch order details
            PreparedStatement detailStmt = connection.prepareStatement("SELECT * FROM order_restaurant_foods WHERE orderID = ?");
            detailStmt.setInt(1, orderId);
            ResultSet detailRs = detailStmt.executeQuery();

            Map<Integer, List<Order.OrderDetail>> restaurantDetails = new HashMap<>();
            while (detailRs.next()) {
                int restaurantId = detailRs.getInt("restaurantID");
                Order.OrderDetail detail = new Order.OrderDetail(detailRs.getInt("foodID"), detailRs.getDouble("price"), detailRs.getInt("quantity"));

                restaurantDetails.computeIfAbsent(restaurantId, k -> new ArrayList<>()).add(detail);
            }

            // Construct Order objects
            List<Order> orders = new ArrayList<>();
            for (Map.Entry<Integer, List<Order.OrderDetail>> entry : restaurantDetails.entrySet()) {
                int restaurantId = entry.getKey();
                List<Order.OrderDetail> details = entry.getValue();

                // Fetch restaurant details (assuming a restaurants table exists)
                PreparedStatement restStmt = connection.prepareStatement("SELECT * FROM restaurants WHERE restaurantID = ?");
                restStmt.setInt(1, restaurantId);
                ResultSet restRs = restStmt.executeQuery();

                if (restRs.next()) {
                    Order.Restaurant restaurant = new Order.Restaurant(restaurantId, restRs.getString("restaurantPhone"), restRs.getString("restaurantAddress"));
                    orders.add(new Order(restaurant, details.toArray(new Order.OrderDetail[0])));
                }
            }

            return new CustomerOrderMessage(customer, orders.toArray(new Order[0]));
        } catch (SQLException e) {
            System.err.println("Query failed: " + e.getMessage());
            return null;
        }
    }

    // Method to insert a CustomerOrderMessage into the database
    public OrderConfirmMessage insertCustomerOrderMessage(CustomerOrderMessage customerOrderMessage) {
        try {
            double totalPrice = calculateOrderTotalPrice(customerOrderMessage);
            int orderId = insertToOrdersTable(customerOrderMessage, totalPrice);
            for (Order order: customerOrderMessage.getOrders()) {
                insertToRestaurantTable(order);
                insertToOrdersRestaurantFoodTable(order, orderId);
            }
            return new OrderConfirmMessage(orderId, totalPrice);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            throw new RuntimeException();
        }
    }

    private void insertToOrdersRestaurantFoodTable(Order order, int orderId) throws SQLException {
        for (Order.OrderDetail orderDetail: order.getOrderDetails()) {
            String insertToOrders = "INSERT INTO order_restaurant_foods(orderID, restaurantID, foodID, price, quantity) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertToOrders);
            pstmt.setInt(1, orderId);
            pstmt.setInt(2, order.getRestaurant().getRestaurantId());
            pstmt.setInt(3, orderDetail.getFoodId());
            pstmt.setDouble(4, orderDetail.getPrice());
            pstmt.setInt(5, orderDetail.getQuantity());
            pstmt.executeUpdate();
        }
    }

    // 3 insert to order_restaurant_foods table
    private void insertToRestaurantTable(Order order) throws SQLException {
        String insertToRestaurants = "INSERT INTO restaurants(restaurantID, restaurantPhone, restaurantAddress) VALUES (?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(insertToRestaurants);
        pstmt.setInt(1, order.getRestaurant().getRestaurantId());
        pstmt.setString(2, order.getRestaurant().getRestaurantPhone());
        pstmt.setString(3, order.getRestaurant().getRestaurantAddress());
        pstmt.executeUpdate();
    }

    // 2 insert to restaurants table
    private int insertToOrdersTable(CustomerOrderMessage customerOrderMessage, double totalPrice) throws SQLException {
        String insertToOrders = "INSERT INTO orders(customerID, customerAddress, customerPhone, totalPrice, status) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement pstmt = connection.prepareStatement(insertToOrders);
        pstmt.setInt(1, customerOrderMessage.getCustomer().getCustomerId());
        pstmt.setString(2, customerOrderMessage.getCustomer().getCustomerAddress());
        pstmt.setString(3, customerOrderMessage.getCustomer().getCustomerPhone());
        pstmt.setDouble(4, totalPrice);
        pstmt.setString(5, "Pending");
        pstmt.executeUpdate();
        ResultSet rs = connection.prepareStatement("select last_insert_rowid();").executeQuery();
        return rs.getInt(1);
    }

    private static double calculateOrderTotalPrice(CustomerOrderMessage customerOrderMessage) {
        // 1 calclulate total price and insert to orders table; get order ID
        double totalPrice = 0;
        for (Order order : customerOrderMessage.getOrders()) {
            for (Order.OrderDetail detail : order.getOrderDetails()) {
                totalPrice += detail.getPrice() * detail.getQuantity();
            }
        }
        return totalPrice;
    }

    // Updates the payment status of an order.
    public boolean updatePaymentStatus(PaymentConfirmMessage paymentConfirmMessage) {
        String updateStatusSql = "UPDATE orders SET status = ? WHERE orderID = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(updateStatusSql)) {
            pstmt.setString(1, paymentConfirmMessage.getStatus());
            pstmt.setInt(2, paymentConfirmMessage.getOrderId());
            int affectedRows = pstmt.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Updating payment status failed: " + e.getMessage());
            return false;
        }
    }
}
