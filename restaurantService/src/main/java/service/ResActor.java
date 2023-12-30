package service;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;
//import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.*;
import com.mongodb.client.model.Sorts;
import ie.foodie.actors.FoodieActor;
import ie.foodie.messages.MenuItemsResponse;
import ie.foodie.messages.RestaurantOrderMessage;
import ie.foodie.messages.RestaurantQueryMessage;
import ie.foodie.messages.RestaurantsResponse;
import ie.foodie.messages.RestaurantsResponse.RestaurantData;
import ie.foodie.messages.models.Order;
import org.bson.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import service.SSE.SSEController;

import java.util.ArrayList;
import java.util.List;

public class ResActor extends FoodieActor {
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final String dbURL = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";
    private String restaurantName = "Unknown";

    private SSEController sseController;
    public ResActor(String dbUrlArg, SSEController sseController) {
        this.sseController = sseController;
        String finalDbUrl = dbUrlArg.length() > 0 ? dbUrlArg : this.dbURL;
        this.mongoClient = MongoClients.create(finalDbUrl);
        this.database = mongoClient.getDatabase("foodie");
        try {
            database.listCollectionNames().first();
            System.out.println("Connected to MongoDB collections successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public ResActor(SSEController sseController) {
        this.sseController = sseController;
        this.mongoClient = MongoClients.create(this.dbURL);
        this.database = mongoClient.getDatabase("foodie");
        try {
            database.listCollectionNames().first();
            System.out.println("Connected to MongoDB collections successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ResActor() {
        this.mongoClient = MongoClients.create(this.dbURL);
        this.database = mongoClient.getDatabase("foodie");
        try {
            database.listCollectionNames().first();
            System.out.println("Connected to MongoDB collections successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFoodName(int foodId) {
        String foodName = "Unknown";
        MongoCollection<Document> collection = database.getCollection("menu_items");
        Document query = new Document("id", foodId);
        Document menuItem = collection.find(query).first();

        if (menuItem != null) {
            foodName = menuItem.getString("name");
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

    private void findRestaurant(int restaurantID) {
        MongoCollection<Document> collection = database.getCollection("restaurants");
        Document query = new Document("id", restaurantID); // assuming 'id' is the field name in MongoDB
        Document restaurant = collection.find(query).first();

        if (restaurant != null) {
            restaurantName = restaurant.getString("name");
        } else {
            System.out.println("No restaurant found with id " + restaurantID);
        }
    }

    public ArrayList<RestaurantData> fetchRestaurants() {
        MongoCollection<Document> collection = database.getCollection("restaurants");
        ArrayList<RestaurantData> restaurantList = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find().sort(Sorts.ascending("id")).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                restaurantList.add(new RestaurantData(
                        doc.getInteger("id"),
                        doc.getString("name"),
                        doc.getString("address"),
                        doc.getString("description"),
                        doc.getString("website")));
            }
            return restaurantList;
        }
    }

    public ArrayList<MenuItemsResponse.MenuItemData> fetchMenuItems(int restaurantId) {
        MongoCollection<Document> collection = database.getCollection("menu_items");
        Document query = new Document("restaurant_id", restaurantId);
        MongoCursor<Document> cursor = collection.find(query).iterator();
        ArrayList<MenuItemsResponse.MenuItemData> menuItemsList = new ArrayList<>();
        try {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                menuItemsList.add(new MenuItemsResponse.MenuItemData(
                        doc.getInteger("id"),
                        doc.getString("name"),
                        doc.getDouble("price"),
                        doc.getString("description")));
            }
        } finally {
            cursor.close();
        }
        return menuItemsList;
    }


    @Override
    public Receive createReceive() {
        System.out.println("\n***Restaurant Service listener has been set up***\n");
        return new ReceiveBuilder()
                .match(RestaurantOrderMessage.class, msg -> {
                    int customerID = msg.getCustomerId();
                    int restaurantID = msg.getOrder().getRestaurant().getRestaurantId();
                    String restaurantAddress = msg.getOrder().getRestaurant().getRestaurantAddress();
                    String restaurantPhone = msg.getOrder().getRestaurant().getRestaurantPhone();
                    findRestaurant(restaurantID);

                    OrderDTO orderDTO = new OrderDTO();
                    orderDTO.setCustomerId(msg.getCustomerId());
                    orderDTO.setRestaurantId(msg.getOrder().getRestaurant().getRestaurantId());
                    orderDTO.setRestaurantName(restaurantName); // Assuming restaurantName is already fetched
                    orderDTO.setRestaurantAddress(msg.getOrder().getRestaurant().getRestaurantAddress());
                    orderDTO.setRestaurantPhone(msg.getOrder().getRestaurant().getRestaurantPhone());

                    System.out.println("Received an order...");
                    System.out.println(repeat("=", 80));
                    System.out.println("+" + repeat("-", 50) + "+");
                    System.out.println("| Order Details" + repeat(" ", 36) + "|");
                    System.out.println("| Customer id: " + customerID
                            + repeat(" ", 36 - Integer.toString(customerID).length()) + "|");
                    System.out.println("| - Restaurant ID     : " + restaurantID
                            + repeat(" ", 27 - Integer.toString(restaurantID).length()) + "|");
                    System.out.println("| - Restaurant Name   : " + restaurantName
                            + repeat(" ", 27 - restaurantName.length()) + "|");
                    System.out.println("| - Restaurant Address: " + restaurantAddress
                            + repeat(" ", 27 - restaurantAddress.length()) + "|");
                    System.out.println("| - Restaurant Phone  : " + restaurantPhone
                            + repeat(" ", 27 - restaurantPhone.length()) + "|");
                    System.out.println("+" + repeat("-", 50) + "+");

                    System.out.println("+" + repeat("-", 77) + "+");
                    System.out.println("| Ordered Items" + repeat(" ", 63) + "|");

                    List<OrderDetailDTO> orderDetails = new ArrayList<>();

                    for (Order.OrderDetail item : msg.getOrder().getOrderDetails()) {
                        String foodName = getFoodName(item.getFoodId());
                        String foodItemString = String.format(
                                " - Food ID: %d, Food Name: %-20s, Price: %5.2f, Quantity: %d",
                                item.getFoodId(), foodName, item.getPrice(), item.getQuantity());
                        System.out.println("| " + foodItemString + repeat(" ", 49 - foodItemString.length()) + "|");

                        OrderDetailDTO detail = new OrderDetailDTO();
                        detail.setFoodId(item.getFoodId());
                        detail.setFoodName(getFoodName(item.getFoodId()));
                        detail.setPrice(item.getPrice());
                        detail.setQuantity(item.getQuantity());
                        orderDetails.add(detail);

                    }
                    orderDTO.setOrderDetails(orderDetails);
                    System.out.println("+" + repeat("-", 77) + "+");
                    getSender().tell("Order received", getSelf());
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonMessage = objectMapper.writeValueAsString(orderDTO);
                    sseController.sendMessageToClients(jsonMessage);
                })
                .match(RestaurantQueryMessage.class, msg -> {
                    ActorRef sender = getSender();
                    System.out.println("Request received from customer service." + sender);
                    if (msg.getQueryType() == RestaurantQueryMessage.QueryType.RESTAURANT_LIST) {
                        System.out.println("Restaurants list is requested.");
                        ArrayList<RestaurantData> restaurantList = fetchRestaurants();
                        sender.tell(new RestaurantsResponse(restaurantList), getSelf());

                        ObjectMapper objectMapper = new ObjectMapper();
                        String jsonMessage = objectMapper.writeValueAsString("Restaurant list sent to user.");
                        sseController.sendMessageToClients(jsonMessage);
                        System.out.println("Restaurant List send back to user service " + sender);
                    } else if (msg.getQueryType() == RestaurantQueryMessage.QueryType.MENU_REQUEST) {
                        System.out.println("Menu list is request for restaurant id: " + msg.getRestaurantID());
                        int restaurantId = msg.getRestaurantID();
                        ArrayList<MenuItemsResponse.MenuItemData> menuItemsList = fetchMenuItems(restaurantId);
                        getSender().tell(new MenuItemsResponse(menuItemsList), getSelf());
                        ObjectMapper objectMapper = new ObjectMapper();
                        String jsonMessage = objectMapper.writeValueAsString("Restaurant ID: " + restaurantId + " menu sent back to user");
                        sseController.sendMessageToClients(jsonMessage);
                    }
                })
                .match(String.class, msg -> {
                    System.out.println("Received message from: " + getSender());
                })
                .build();
    }
    public static class OrderDetailDTO {
        private int foodId;
        private String foodName;
        private double price;
        private int quantity;

        // Constructors
        public OrderDetailDTO() {
        }

        public OrderDetailDTO(int foodId, String foodName, double price, int quantity) {
            this.foodId = foodId;
            this.foodName = foodName;
            this.price = price;
            this.quantity = quantity;
        }

        // Getters and setters
        public int getFoodId() {
            return foodId;
        }

        public void setFoodId(int foodId) {
            this.foodId = foodId;
        }

        public String getFoodName() {
            return foodName;
        }

        public void setFoodName(String foodName) {
            this.foodName = foodName;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class OrderDTO {
        private int customerId;
        private int restaurantId;
        private String restaurantName;
        private String restaurantAddress;
        private String restaurantPhone;
        private List<OrderDetailDTO> orderDetails;

        // Constructors
        public OrderDTO() {
        }

        public OrderDTO(int customerId, int restaurantId, String restaurantName,
                        String restaurantAddress, String restaurantPhone,
                        List<OrderDetailDTO> orderDetails) {
            this.customerId = customerId;
            this.restaurantId = restaurantId;
            this.restaurantName = restaurantName;
            this.restaurantAddress = restaurantAddress;
            this.restaurantPhone = restaurantPhone;
            this.orderDetails = orderDetails;
        }

        // Getters and setters
        public int getCustomerId() {
            return customerId;
        }

        public void setCustomerId(int customerId) {
            this.customerId = customerId;
        }

        public int getRestaurantId() {
            return restaurantId;
        }

        public void setRestaurantId(int restaurantId) {
            this.restaurantId = restaurantId;
        }

        public String getRestaurantName() {
            return restaurantName;
        }

        public void setRestaurantName(String restaurantName) {
            this.restaurantName = restaurantName;
        }

        public String getRestaurantAddress() {
            return restaurantAddress;
        }

        public void setRestaurantAddress(String restaurantAddress) {
            this.restaurantAddress = restaurantAddress;
        }

        public String getRestaurantPhone() {
            return restaurantPhone;
        }

        public void setRestaurantPhone(String restaurantPhone) {
            this.restaurantPhone = restaurantPhone;
        }

        public List<OrderDetailDTO> getOrderDetails() {
            return orderDetails;
        }

        public void setOrderDetails(List<OrderDetailDTO> orderDetails) {
            this.orderDetails = orderDetails;
        }
    }

}
