import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import ie.foodie.messages.MenuItemsResponse;
import ie.foodie.messages.RestaurantsResponse;
import ie.foodie.messages.models.CustomerV2;
import ie.foodie.messages.models.Order;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletionStage;

public class Main {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static String dbURL = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";
    private static MongoCollection<Document> collection;
    private static int TOKEN = 1;
    private static HashMap<Integer, String> userList = new HashMap<>();
    private static WebSocket webSocket;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static ArrayList<Order.OrderDetail> orderDetail = new ArrayList<>();
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
                                switch (jsonObject.getString("msgType")) {
                                    case "RestaurantsResponse":
//                                        try {
//                                            RestaurantsResponse restaurantsResponse = objectMapper.readValue(message, RestaurantsResponse.class);
//                                            List<RestaurantsResponse.RestaurantData> restaurantList = restaurantsResponse.getRestaurants();
//                                            for (RestaurantsResponse.RestaurantData restaurant : restaurantList) {
//                                                System.out.println(restaurant.toString());}
//                                            System.out.println("Enter a restaurant id to check its menu.\n");
//                                            int reStaurantId = scanner.nextInt();
//                                            if ((reStaurantId > 0) && (reStaurantId < 31)) {
//                                                String msg = "{\"msgType\" : \"selectRestaurant\", " +
//                                                        "\"uuid\" : " + user.getUuid() + ", " +
//                                                        "\"message\" : " + reStaurantId;
//                                                webSocket.sendText(msg, true);}
//                                            for (RestaurantsResponse.RestaurantData restaurant : restaurantList) {
//                                                if (restaurant.getId() == reStaurantId) {
//                                                    restaurantId = restaurant.getId();
//                                                    restaurantPhone = restaurant.getName();
//                                                    restaurantAddress = restaurant.getAddress();
//                                                }
//                                            }
//                                        } catch (JsonProcessingException e) {
//                                            throw new RuntimeException(e);
//                                        }
                                        break;
                                    case "MenuItemsResponse":
                                        try {
                                            MenuItemsResponse menuItemsResponse = objectMapper.readValue(message, MenuItemsResponse.class);
                                            ArrayList<MenuItemsResponse.MenuItemData> menuList = menuItemsResponse.getMenuItems();
                                            for (MenuItemsResponse.MenuItemData food : menuList) {
                                                System.out.println(food.toString());
                                            }
                                            System.out.println("Enter the Id of the food you want to order: ");
                                            int foodId = scanner.nextInt();
                                            String msg = "{\"msgType\" : \"selectFood\", " +
                                                    "\"uuid\" : " + user.getUuid() + ", " +
                                                    "\"message\" : " + foodId;
                                            webSocket.sendText(msg, true);

                                            String continueOrder;
                                            do {
                                                boolean orderHandled = false;
                                                System.out.print("Enter the Id of the food you want to order: ");
                                                for (Order.OrderDetail food : orderDetail) {
                                                    if (foodId == food.getFoodId()) {
                                                        food.setQuantity(food.getQuantity() + 1);
                                                        orderHandled = true;
                                                    }
                                                }
                                                if (!orderHandled) {
                                                    for (MenuItemsResponse.MenuItemData food : menuList) {
                                                        if (foodId == food.getItemId()) {
                                                            orderDetail.add(new Order.OrderDetail(food.getItemId(), food.getPrice(), 1));
                                                        }
                                                    }
                                                }
                                                System.out.println("Order added to your cart!");
                                                System.out.println("Current cart:");
                                                for (Order.OrderDetail food : orderDetail) {
                                                    System.out.println("FoodID: " + food.getFoodId() + " Price: " + food.getPrice()
                                                            + " Quantity: " + food.getQuantity());
                                                }
                                                System.out.println(
                                                        "Enter 'Yes' to continue ordering, or 'No' to finish and send the order.");
                                                continueOrder = scanner.nextLine();
                                                String msg2 = "{\"msgType\" : \"continueOrder\", " +
                                                        "\"uuid\" : " + user.getUuid() + ", " +
                                                        "\"message\" : " + continueOrder;
                                                webSocket.sendText(msg2, true);
                                            }
                                            while (!continueOrder.toLowerCase().equals("no"));
                                            System.out.println("user make an order to order system");
//                                            Order.Restaurant restaurant = new Order.Restaurant(restaurantId[0], restaurantPhone[0],
//                                                    restaurantAddress[0]);
//                                            ArrayList<Order> order = new ArrayList<>();
//                                            order.add(new Order(restaurant, orderDetail));
                                        } catch (JsonProcessingException e) {
                                            throw new RuntimeException(e);
                                        }
                                        break;
                                    case "":
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
