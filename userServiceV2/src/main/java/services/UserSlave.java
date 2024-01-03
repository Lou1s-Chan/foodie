package services;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import ie.foodie.actors.FoodieActor;
import ie.foodie.messages.*;
import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.CustomerV2;
import ie.foodie.messages.models.Order;
import org.bson.Document;
import org.bson.conversions.Bson;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.http.WebSocket;
import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

public class UserSlave extends FoodieActor {

    private int customerId;
    private String customerAddress;
    private String customerPhone;
    private int restaurantId;
    private String restaurantAddress;
    private String restaurantPhone;
    private ArrayList<Order.OrderDetail> orderDetail = new ArrayList<>();
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static String dbURL = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";
    private static MongoCollection<Document> collection;
    private static WebSocket webSocket;
//    private Scanner scanner = new Scanner(System.in);
    private Integer reStaurantId;
    private Integer foodId;
    private String continueOrder;
    private String payWithSavedCard;
    private String payMethod;
    private String useNewCard;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(ClientMessage.class, clientMessage -> {
                    switch (clientMessage.getMsgType()) {
                        case "selectRestaurant":
                            this.reStaurantId = Integer.valueOf(clientMessage.getMessage());
                            break;
                        case "selectFood":
                            this.foodId = Integer.valueOf(clientMessage.getMessage());
                            break;
                        case "continueOrder":
                            this.continueOrder = clientMessage.getMessage();
                            break;
                        case "payMethod":
                            this.payMethod = clientMessage.getMessage();
                            break;
                        case "payWithSavedCard":
                            this.payWithSavedCard = clientMessage.getMessage();
                            break;
                        case "useNewCard":
                            this.useNewCard = clientMessage.getMessage();
                            break;
                    }
                })
                .match(CustomerV2.class, customerV2 -> {
                    this.customerId = customerV2.getCustomerId();
                    customerAddress = customerV2.getCustomerAddress();
                    customerPhone = customerV2.getCustomerPhone();
                })
                .match(RestaurantsResponse.class,
                        msg -> {
                            System.out.println("Received back Restaurant Response...");
                            List<RestaurantsResponse.RestaurantData> restaurantList = msg.getRestaurants();
                            for (RestaurantsResponse.RestaurantData restaurant : restaurantList) {
                                System.out.println(restaurant.toString());
                                webSocket.sendText(JsonUtil.serialize(msg),true);
                            }
//                            while (true) {
                                System.out.print("Enter a restaurant id to check its menu: ");
                                System.out.println(reStaurantId);

                                // Check if the next input is an integer
//                                if (scanner.hasNextInt()) {
                                int selectedRestaurantId = reStaurantId;// scanner.nextInt();
//                                if ((selectedRestaurantId > 0) && (selectedRestaurantId < 31)) {
//                                    scanner.nextLine();
                                    getSender().tell(
                                            new RestaurantQueryMessage(
                                                    RestaurantQueryMessage.QueryType.MENU_REQUEST,
                                                    selectedRestaurantId, getSelf()),
                                            getSelf());
                                    for (RestaurantsResponse.RestaurantData restaurant : restaurantList) {
                                        if (restaurant.getId() == selectedRestaurantId) {
                                            restaurantId = restaurant.getId();
                                            restaurantPhone = restaurant.getName();
                                            restaurantAddress = restaurant.getAddress();
                                        }
                                    }
                                    System.out.println("Menu request sent!");
//                                    break; // Exit the loop if a valid integer is entered
//                                } else {
//                                    System.out.println("Invalid input. Please enter a valid integer.");
//                                    scanner.nextLine(); // Consume the invalid input to avoid an infinite loop
//                                }
//                                }
//                            }
                        })
                .match(MenuItemsResponse.class,
                        msg -> {
                            System.out.println("Received back menu Response...");
                            ArrayList<MenuItemsResponse.MenuItemData> menuList = msg.getMenuItems();
                            for (MenuItemsResponse.MenuItemData food : menuList) {
                                System.out.println(food.toString());
                                webSocket.sendText(JsonUtil.serialize(msg), true);
                            }

                            do {
                                boolean orderHandled = false;
                                System.out.print("Enter the Id of the food you want to order: ");
                                int userInput = foodId;//scanner.nextInt();
//                                scanner.nextLine();
                                for (Order.OrderDetail food : orderDetail) {
                                    if (userInput == food.getFoodId()) {
                                        food.setQuantity(food.getQuantity() + 1);
                                        orderHandled = true;
                                    }
                                }
                                if (!orderHandled) {
                                    for (MenuItemsResponse.MenuItemData food : menuList) {
                                        if (userInput == food.getItemId()) {
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
                                System.out.println(continueOrder);
                            } while (!continueOrder.toLowerCase().equals("no"));//while (!scanner.nextLine().toLowerCase().equals("no"));
                            ActorSystem system = getContext().getSystem();
                            ActorSelection orderSystem = system
                                    .actorSelection("akka.tcp://order-system@localhost:2553/user/order-service");
                            System.out.println("user make an order to order system");

                            Order.Restaurant restaurant = new Order.Restaurant(restaurantId, restaurantPhone,
                                    restaurantAddress);

                            ArrayList<Order> order = new ArrayList<>();
                            order.add(new Order(restaurant, orderDetail));
                            orderSystem.tell(new CustomerOrderMessage(
                                    new Customer(customerId, customerAddress, customerPhone), order), getSelf());
                        })
                .match(OrderConfirmMessage.class, this::requestPayment)
                .match(PaymentStatusMessage.class, msg -> {
                    System.out.println("Payment Status for Order ID " + msg.getOrderId() + ": "
                            + msg.getStatus() + " - " + msg.getMessage());
                })
                .match(DeliveryQueryMessage.class, msg -> {
                    switch (msg.getStatus()) {
                        case "Pending":
                            System.out.println("We are finding suitable driver for order: " + msg.getOrderId() + ".\n");
                            break;
                        case "NoDriver":
                            System.out.println("There no suitable driver for order: " + msg.getOrderId() + ".\n"
                                    + "But we will try our best to allocate one.");
                            break;
                        case "Dispatched":
                        case "Delivered":
                            System.out.println(msg.getMessage());
                            break;
                    }
                })
                .build();
    }

    private void requestPayment(OrderConfirmMessage msg){
        ActorSystem system = getContext().getSystem();
        ActorSelection orderSystem = system
                .actorSelection("akka.tcp://order-system@localhost:2553/user/order-service");

        double amountToPay = msg.getTotalPrice();
        int orderId = msg.getOrderId();

        System.out.print("Enter payment method (Card/Cash): ");
//        String paymentMethod = scanner.nextLine().trim();
        System.out.println(payMethod);
        String paymentMethod = payMethod;

        if (!paymentMethod.equalsIgnoreCase("Card") && !paymentMethod.equalsIgnoreCase("Cash")) {
            System.out.println("Invalid input. Defaulting to Card.");
            paymentMethod = "Card";
        }

        if (paymentMethod.equalsIgnoreCase("Card")) {
            String encryptedCardNumber = getEncryptedCardNumber(this.customerId);
            String cardNumber = null;

            if (encryptedCardNumber != null) {
                try {
                    String decryptedCardNumber = decryptCardNumber(encryptedCardNumber);
                    System.out.println("Your saved card number is: " + maskedCardNumber(decryptedCardNumber));
                    System.out.print("Do you want to use this card? (yes/no): ");
                    System.out.println(payWithSavedCard);
//                    String response = scanner.nextLine().trim();
                    String response = payWithSavedCard;
                    if (response.equalsIgnoreCase("yes")) {
                        cardNumber = decryptedCardNumber;
                    } else {
                        cardNumber = getCardNumberFromUser(); // Call to get new card number
                        encryptedCardNumber = encryptCardNumber(cardNumber); // Encrypt the new card number
                        updateUserCardNumberInDatabase(this.customerId, encryptedCardNumber); // Update the database
                    }
                } catch (IllegalArgumentException e) {
                    cardNumber = getCardNumberFromUser(); // Call to get new card number
                    encryptedCardNumber = encryptCardNumber(cardNumber); // Encrypt the new card number
                    updateUserCardNumberInDatabase(this.customerId, encryptedCardNumber); // Update the database
                }
            } else {
                cardNumber = getCardNumberFromUser(); // Call to get new card number
                encryptedCardNumber = encryptCardNumber(cardNumber); // Encrypt the new card number
                updateUserCardNumberInDatabase(this.customerId, encryptedCardNumber); // Update the database
            }

            if (cardNumber != null) {
                OrderPaymentMessage paymentMessage = new OrderPaymentMessage(orderId, this.customerId, amountToPay, paymentMethod, getSelf());
                orderSystem.tell(paymentMessage, getSelf());
            } else {
                System.out.println("Payment process aborted due to missing card number.");
            }
        } else {
            OrderPaymentMessage paymentMessage = new OrderPaymentMessage(orderId, this.customerId, amountToPay, paymentMethod, getSelf());
            orderSystem.tell(paymentMessage, getSelf());
        }
    }


    private String getCardNumberFromUser() {
        System.out.print("Please enter your card number: ");
        System.out.println(useNewCard);
//        return scanner.nextLine().trim();
        return useNewCard;
    }


    private String getEncryptedCardNumber(int customerId) {
        String customerIdStr = String.format("%03d", customerId); // Assuming customerId is formatted as a string with leading zeros

        mongoClient = MongoClients.create(dbURL);
        database = mongoClient.getDatabase("foodie");
        collection = database.getCollection("users");

        Bson filter = Filters.eq("ID", customerIdStr);
        Document userDocument = collection.find(filter).first();

        if (userDocument != null) {
            // Directly check if the retrieved card number is null
            String cardNumber = userDocument.getString("cardNumber");
            if (cardNumber != null && !cardNumber.isEmpty()) {
                return cardNumber;
            }
        }
        return null; // Return null if the document is not found or cardNumber is null
    }

    private static final String AES = "AES";
    private static final byte[] key = new byte[] { 'T', 'h', 'e', 'B', 'e', 's', 't', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y' };

    private String encryptCardNumber(String cardNumber) {
        try {
            Key aesKey = new SecretKeySpec(key, AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] encrypted = cipher.doFinal(cardNumber.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String decryptCardNumber(String encryptedCardNumber) {
        try {
            // Validate if the encrypted string is Base64 encoded
            if (Base64.getDecoder().decode(encryptedCardNumber) == null) {
                System.out.println("Invalid Base64 encoding");
                return null;
            }

            Key aesKey = new SecretKeySpec(key, AES);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedCardNumber));
            return new String(decrypted);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private String maskedCardNumber(String cardNumber) {
        // Return a masked version of the card number, like "**** **** **** 1234"
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private void updateUserCardNumberInDatabase(int customerId, String encryptedCardNumber) {
        String customerIdStr = String.format("%03d", customerId);  // Convert to a string like "001"

        Bson filter = Filters.eq("ID", customerIdStr);
        Bson updateOperation = Updates.set("cardNumber", encryptedCardNumber);
        collection.updateOne(filter, updateOperation);
    }

    public static class JsonUtil {
        private static final ObjectMapper objectMapper = new ObjectMapper();

        public static String serialize(Object obj) throws IOException {
            return objectMapper.writeValueAsString(obj);
        }

        public static <T> T deserialize(String json, Class<T> clazz) throws IOException {
            return objectMapper.readValue(json, clazz);
        }
    }

    public static Props props() {
        return Props.create(UserSlave.class);
    }
}
