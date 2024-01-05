package service;

import java.util.HashMap;
import java.util.Scanner;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ie.foodie.messages.RestaurantQueryMessage;
import ie.foodie.messages.models.Customer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import service.SSE.SSEController;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
public class Main {

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static String dbURL = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";
    private static MongoCollection<Document> collection;
    private static int TOKEN = 1;
    private static HashMap<Integer, String> userList = new HashMap<>();

    public static void main(String[] args) {

        //
        ApplicationContext applicationContext = SpringApplication.run(Main.class, args);
        SSEController sseController = applicationContext.getBean(SSEController.class);
        //

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
        // final ActorRef ref = system.actorOf(Props.create(UserActor.class),
        // "user-service");
        final ActorRef ref = system.actorOf(Props.create(UserActor.class, () -> new UserActor(system, sseController)), "user-service");

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
                                "/user/user-service");
                selection1.tell(user, ref);
            } else {
                System.out.println("Invalid username or password. Please try again.");
            }

        }

        // once user successfully login, send message to RESTAURANT for restaunt list
        ActorSelection selection2 = system
                .actorSelection("akka.tcp://order-system@order-service:2553/user/order-service");
        System.out.println("user make a query to restaurant system (through order service)");
        selection2.tell(new RestaurantQueryMessage(RestaurantQueryMessage.QueryType.RESTAURANT_LIST, ref), ref);

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
        int id = Integer.parseInt(userDocument.getString("ID"));
        String address = userDocument.getString("address");
        String phone = userDocument.getString("telephone");
        userList.put(id, username);
        Customer user = new Customer(id, address, phone);
        return user;

    }

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static MongoDatabase getDatabase() {
        return database;
    }
}
