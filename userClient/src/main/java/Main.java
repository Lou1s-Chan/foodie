import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import ie.foodie.messages.models.CustomerV2;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class Main {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static String dbURL = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";
    private static MongoCollection<Document> collection;
    private static int TOKEN = 1;
    private static HashMap<Integer, String> userList = new HashMap<>();
    private static WebSocket webSocket;
    public static void main (String[] args) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();

        // connect to MongoDB
        mongoClient = MongoClients.create(dbURL);
        database = mongoClient.getDatabase("foodie");
        collection = database.getCollection("users");



        try {
            System.out.println("Connected to MongoDB collections successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        boolean loginSuccess = false;
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to Foodie!");
        while (!loginSuccess) {
            // Ask the user to enter username and password
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();
            boolean exist = checkLogin(username, password);
            if (exist) {
                System.out.println("Login success!");
                loginSuccess = true;
                // Initiate the customer with all details from the database
                CustomerV2 user = getUserDetails(username);
                String cuStomer = "{\"msgType\" : \"CustomerDetails\", " +
                        "\"customerId\" : " + user.getCustomerId() + ", " +
                        "\"customerAddress\" : " + user.getCustomerAddress() + ", " +
                        "\"customerPhone\" : " + user.getCustomerPhone() + ", " +
                        "\"uuid\" : " + user.getUuid() + "}";
                webSocket = httpClient.newWebSocketBuilder()
                        .buildAsync(URI.create("ws://localhost:4321/foodie"), new WebSocket.Listener() {
                            @Override
                            public void onOpen(WebSocket webSocket) {
                                WebSocket.Listener.super.onOpen(webSocket);
                                webSocket.sendText(cuStomer, true);
                            }

                            @Override
                            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                                String message = data.toString();
                                JSONObject jsonObject = new JSONObject(message);
                                if (!jsonObject.has("msgType")){
                                    System.out.println(data);
                                    System.out.println("Enter a restaurant id to check its menu.\n");
                                    int reStaurantId = Integer.parseInt(scanner.nextLine());
                                    if ((reStaurantId > 0) && (reStaurantId < 31)) {
                                        String msg = "{\"msgType\" : \"selectRestaurant\", " +
                                                "\"uuid\" : " + user.getUuid() + ", " +
                                                "\"message\" : " + reStaurantId;
                                        webSocket.sendText(msg, true);
                                    }
                                } else {
                                    switch (jsonObject.getString("msgType")) {
                                        case "":
                                    }
                                }
                                return null;
                            }

                            @Override
                            public String toString() {
                                return super.toString();
                            }
                        }).join();
            }
            else {
                System.out.println("Invalid username or password. Please try again.");
            }

        }

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
    private static CustomerV2 getUserDetails(String username) {
        Bson query = Filters.eq("username", username);
        String uuid = UUID.randomUUID().toString().replace("-", "");

        // Execute the query
        Document userDocument = collection.find(query).first();
        int id = Integer.parseInt(userDocument.getString("ID"));
        String address = userDocument.getString("address");
        String phone = userDocument.getString("telephone");
        userList.put(id, username);
        return new CustomerV2("CustomerDetails", id, address, phone, uuid);

    }
//
//    public static class JsonUtil {
//        private static final ObjectMapper objectMapper = new ObjectMapper();
//
//        public static String serialize(Object obj) throws IOException {
//            return objectMapper.writeValueAsString(obj);
//        }
//
//        public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
//            return objectMapper.readValue(json, clazz);
//        }
//    }
}
