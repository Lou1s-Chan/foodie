import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Scanner;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

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

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static String dbURL = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";
    private static MongoCollection<Document> collection;
    private static int TOKEN = 1;
    private static HashMap<Integer, String> userList = new HashMap<>();

    public static void main(String[] args) {

        // connect to MongoDB
        mongoClient = MongoClients.create(dbURL);
        database = mongoClient.getDatabase("foodie");
        collection = database.getCollection("users");

        try {
            System.out.println("Connected to MongoDB collections successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // creating the system and actor for USER
        ActorSystem system = ActorSystem.create("user-system");
        final ActorRef ref = system.actorOf(Props.create(UserActor.class), "user-service");

        boolean loginSuccess = false;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Foodie!");
        while (!loginSuccess) {
            // Ask the user to enter username and password
            System.out.print("Enter your username(Moo): ");
            String username = scanner.nextLine();
            System.out.print("Enter your password(password1): ");
            String password = scanner.nextLine();
            boolean exist = checkLogin(username, password);
            if (exist) {
                System.out.println("Login success!");
                loginSuccess = true;
                // Initiate the customer with all details from the database
                Customer user = getUserDetails(username);
                ActorSelection selection1 = system
                        .actorSelection(
                                "akka.tcp://user-system@localhost:2552/user/user-service");
                selection1.tell(user, ref);
            } else {
                System.out.println("Invalid username or password. Please try again.");
            }

        }

        // once user successfully login, send message to RESTAURANT for restaunt list
        ActorSelection selection2 = system
                .actorSelection("akka.tcp://restaurant-system@localhost:2551/user/restaurant-service");
        System.out.println("user make a query to restaurant system");
        selection2.tell(new RestaurantQueryMessage(RestaurantQueryMessage.QueryType.RESTAURANT_LIST), ref);

    }

    private static Boolean checkLogin(String username, String password) {
        // Query to find documents with the specified username and password
        Bson query = Filters.and(
                Filters.eq("username", username),
                Filters.eq("password", password));

        // Execute the query
        Document userDocument = collection.find(query).first();

        // Check if a document with the specified username and password was found
        return userDocument != null;
    }

    // // get user details from database
    private static Customer getUserDetails(String username) {
        Bson query = Filters.eq("username", username);

        // Execute the query
        Document userDocument = collection.find(query).first();
        String address = userDocument.getString("address");
        String phone = userDocument.getString("telephone");
        userList.put(TOKEN, username);
        Customer user = new Customer(TOKEN++, address, phone);
        return user;

    }
}
