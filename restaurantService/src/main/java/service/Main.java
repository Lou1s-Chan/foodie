package service;

import akka.actor.*;
import java.sql.*;
import service.ResQuoter;

public class Main {
    static ActorSystem system = ActorSystem.create("RestaurantService");
    public static void main(String[] args) {
        System.out.println("Running Restaurant Service");

        final Props ResQuoterProp = Props.create(ResQuoter.class);
        final ActorRef ResQuoterRef = system.actorOf(ResQuoterProp, "ResQuoter");

        String OrderPath = "akka.tcp://default@order:2550/user/OrderQuoter";
        ActorSelection remoteActor = system.actorSelection(OrderPath);

        System.out.println("remoteActor: " + remoteActor);
//        remoteActor.tell(new Message(ResQuoterRef, "RestaurantService"), ResQuoterRef);


        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        String url = "jdbc:sqlite:restaurantService/database/restaurantdatabase.db";
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                listRestaurants(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static void listRestaurants(Connection conn) {
        String sqlGetRestaurants = "SELECT id, name FROM restaurants;";  // Adjusted to include 'name'

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlGetRestaurants)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");  // Retrieves the 'name' column value
                System.out.println("Restaurant ID: " + id + ", Name: " + name);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
