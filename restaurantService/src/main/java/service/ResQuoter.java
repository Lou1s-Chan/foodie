package service;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import ie.foodie.messages.RestaurantOrderMessage;
import ie.foodie.messages.models.Order;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

public class ResQuoter extends AbstractActor {
    private String restaurantName = "Unknown";
    private final HikariDataSource ds;

    public ResQuoter() {
        this.ds = new HikariDataSource();
        this.ds.setJdbcUrl("jdbc:sqlite:database/restaurantdatabase.db");
    }

    private String getFoodName(int foodId) throws SQLException {
        String foodName = "Unknown";
        String query = "SELECT name FROM menu_item WHERE id = ?";
        try (Connection conn = ds.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, foodId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foodName = rs.getString("name");
                }
            }
        }
        return foodName;
    }

    public static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }


    @Override
    public Receive createReceive() {
        System.out.println("\n***Restaurant Service listener has been set up***\n");
        return new ReceiveBuilder().match(
                RestaurantOrderMessage.class, msg -> {
                    int customerID = msg.getCustomerId();
                    int restaurantID = msg.getOrder().getRestaurant().getRestaurantId();
                    String restaurantAddress = msg.getOrder().getRestaurant().getRestaurantAddress();
                    String restaurantPhone = msg.getOrder().getRestaurant().getRestaurantPhone();


                    try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
                        String query = "SELECT * FROM restaurants WHERE id =" + restaurantID;
                        try (ResultSet rs = stmt.executeQuery(query)) {
                            if (rs.next()) {
                                restaurantName = rs.getString("name");
                            } else {
                                System.out.println("No restaurant found with id " + restaurantID);
                            }
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Received an order...");
                    System.out.println(repeat("=", 80));
                    System.out.println("+" + repeat("-", 50) + "+");
                    System.out.println("| Order Details" + repeat(" ",36) + "|");
                    System.out.println("| Customer id: " + customerID + repeat(" ", 36 - Integer.toString(customerID).length()) + "|");
                    System.out.println("| - Restaurant ID     : " + restaurantID + repeat(" ", 27 - Integer.toString(restaurantID).length()) + "|");
                    System.out.println("| - Restaurant Name   : " + restaurantName + repeat(" ", 27 - restaurantName.length()) + "|");
                    System.out.println("| - Restaurant Address: " + restaurantAddress + repeat(" ", 27 - restaurantAddress.length()) + "|");
                    System.out.println("| - Restaurant Phone  : " + restaurantPhone + repeat(" ", 27 - restaurantPhone.length()) + "|");
                    System.out.println("+" + repeat("-", 50) + "+");

                    System.out.println("+" + repeat("-",77) + "+");
                    System.out.println("| Ordered Items" + repeat(" ",63) + "|");
                    for (Order.OrderDetail item : msg.getOrder().getOrderDetails()) {
                        String foodName = "Unknown";
                        try {
                            foodName = getFoodName(item.getFoodId());
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        String foodItemString = String.format(" - Food ID: %d, Food Name: %-20s, Price: %5.2f, Quantity: %d",
                                item.getFoodId(), foodName, item.getPrice(), item.getQuantity());
                        System.out.println("| " + foodItemString + repeat(" ", 49 - foodItemString.length()) + "|");
                    }
                    System.out.println("+" + repeat("-", 77) + "+");
                    getSender().tell("Order received", getSelf());
                }

        ).build();
    }
}
