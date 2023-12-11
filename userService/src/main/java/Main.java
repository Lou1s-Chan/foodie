import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ie.foodie.messages.CustomerOrderMessage;
import ie.foodie.messages.RestaurantQueryMessage;
import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;
import ie.foodie.messages.models.Order.OrderDetail;
import ie.foodie.messages.models.Order.Restaurant;
import service.UserActor;

public class Main {
    public static void main(String[] args) {
        boolean loginSuccess = false;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Foodie!");
        while (!loginSuccess) {
            // Ask the user to enter username and password
            System.out.print("Enter your username(Moo): ");
            String username = scanner.nextLine();
            System.out.print("Enter your password(password1): ");
            String password = scanner.nextLine();

            // Compare user input to the database
            String url = "jdbc:sqlite:userService/database/userdatabase.db";

            try (Connection conn = DriverManager.getConnection(url)) {
                if (conn != null) {
                    HashMap<String, String> nameAndPassword = getNameAndPassword(conn);

                    // Check if the entered credentials are valid
                    for (Map.Entry<String, String> entry : nameAndPassword.entrySet()) {
                        if (username.equals(entry.getKey()) && password.equals(entry.getValue())) {
                            System.out.println("Login success!");
                            loginSuccess = true;
                            // Initiate the customer with all details from the database
                            Customer user = getUserDetails(conn, entry.getKey());
                        }
                    }

                    if (!loginSuccess) {
                        System.out.println("Invalid username or password. Please try again.");
                    }

                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // creating the system and actor for USER
        ActorSystem system = ActorSystem.create("user-system");
        final ActorRef ref = system.actorOf(Props.create(UserActor.class), "user-service");

        // V0.0.3 once user successfully login, send message to RESTAURANT for menu
        ActorSelection selection1 = system
                .actorSelection("akka.tcp://restaurant-system@localhost:2551/user/restaurant-service");
        System.out.println("user make a query to restaurant system");
        // selection1.tell(new
        // RestaurantQueryMessage(RestaurantQueryMessage.QueryType.MENU_REQUEST, 1),
        // ref);
        selection1.tell(new RestaurantQueryMessage(RestaurantQueryMessage.QueryType.RESTAURANT_LIST), ref);

        // // send message to ORDER
        // ActorSelection selection2 = system
        // .actorSelection("akka.tcp://order-system@localhost:2553/user/order-service");
        // System.out.println("user make an order to order system");
        // // V0.0.2 instantiate the restaurants object myself
        // Restaurant restaurant1 = new Restaurant(1, "123456789", "Dublin1");
        // OrderDetail[] orderDetail1 = { new OrderDetail(1, 18.88, 3), new
        // OrderDetail(2, 28.88, 5) };
        // Restaurant restaurant2 = new Restaurant(2, "987654321", "Dublin2");
        // OrderDetail[] orderDetail2 = { new OrderDetail(3, 7.99, 2), new
        // OrderDetail(4, 59.99, 4) };
        // Order[] order1 = { new Order(restaurant1, orderDetail1), new
        // Order(restaurant2, orderDetail2) };
        // selection2.tell(new CustomerOrderMessage(new Customer(1, "Dublin1",
        // "123456789"), order1), ref);
    }

    // get all username and password from database
    private static HashMap<String, String> getNameAndPassword(Connection conn) throws SQLException {
        HashMap<String, String> result = new HashMap<>();
        String query = "SELECT username, password FROM user";
        try (PreparedStatement preparedStatement = conn.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");
                result.put(username, password);
            }
        }
        return result;
    }

    // get all user details from database
    private static Customer getUserDetails(Connection conn, String username) throws SQLException {
        String query = "SELECT user_id, address, phone FROM user WHERE username = ?";

        try (PreparedStatement preparedStatement = conn.prepareStatement(query)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    // User found, extract data and create a Customer object
                    int userId = resultSet.getInt("user_id");
                    String address = resultSet.getString("address");
                    String phone = resultSet.getString("phone");

                    return new Customer(userId, address, phone);
                } else {
                    // User not found
                    return null;
                }
            }
        }
    }
}
