package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import ie.foodie.messages.CustomerOrderMessage;
import ie.foodie.messages.MenuItemsResponse;
import ie.foodie.messages.OrderConfirmMessage;
import ie.foodie.messages.OrderPaymentMessage;
import ie.foodie.messages.PaymentStatusMessage;
import ie.foodie.messages.RestaurantQueryMessage;
import ie.foodie.messages.RestaurantsResponse;
import ie.foodie.messages.RestaurantsResponse.RestaurantData;
import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;
import ie.foodie.messages.models.Order.OrderDetail;
import ie.foodie.messages.models.Order.Restaurant;

public class UserActor extends AbstractActor {
    private int customerId;
    private String customerAddress;
    private String customerPhone;
    private int restaurantId;
    private String restaurantAddress;
    private String restaurantPhone;
    private ArrayList<OrderDetail> orderDetail = new ArrayList<>();
    private Scanner scanner = new Scanner(System.in);

    private ActorSelection paymentServiceActor;

    public UserActor(ActorSystem system) {
        this.paymentServiceActor = system
                .actorSelection("akka.tcp://payment-system@localhost:2555/user/payment-service");
    }

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static String dbURL = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";
    private static MongoCollection<Document> collection;

    @Override
    public Receive createReceive() {
        return new ReceiveBuilder()
                .match(Customer.class,
                        msg -> {
                            this.customerId = msg.getCustomerId();
                            customerAddress = msg.getCustomerAddress();
                            customerPhone = msg.getCustomerPhone();
                        })
                .match(OrderConfirmMessage.class, this::requestPayment)
                .match(PaymentStatusMessage.class, msg -> {
                    // Handle the payment status message
                    System.out.println("Payment Status for Order ID " + msg.getOrderId() + ": "
                            + msg.getStatus() + " - " + msg.getMessage());
                    // Additional logic based on the payment status
                })
                .match(RestaurantsResponse.class,
                        msg -> {
                            System.out.println("Received back Restaurant Response...");
                            List<RestaurantData> restaurantList = msg.getRestaurants();
                            for (RestaurantData restaurant : restaurantList) {
                                System.out.println(restaurant.toString());
                            }
                            while (true) {
                                System.out.print("Enter a restaurant id to check its menu: ");

                                // Check if the next input is an integer
                                if (scanner.hasNextInt()) {
                                    int selectedRestaurantId = scanner.nextInt();
                                    if ((selectedRestaurantId > 0) && (selectedRestaurantId < 31)) {
                                        scanner.nextLine();
                                        getSender().tell(
                                                new RestaurantQueryMessage(
                                                        RestaurantQueryMessage.QueryType.MENU_REQUEST,
                                                        selectedRestaurantId),
                                                getSelf());
                                        for (RestaurantData restaurant : restaurantList) {
                                            if (restaurant.getId() == selectedRestaurantId) {
                                                restaurantId = restaurant.getId();
                                                restaurantPhone = restaurant.getName();
                                                restaurantAddress = restaurant.getAddress();
                                            }
                                        }
                                        System.out.println("Menu request sent!");
                                        break; // Exit the loop if a valid integer is entered
                                    } else {
                                        System.out.println("Invalid input. Please enter a valid integer.");
                                        scanner.nextLine(); // Consume the invalid input to avoid an infinite loop
                                    }
                                }
                            }
                        })
                .match(MenuItemsResponse.class,
                        msg -> {
                            System.out.println("Received back menu Response...");
                            ArrayList<MenuItemsResponse.MenuItemData> menuList = msg.getMenuItems();
                            for (MenuItemsResponse.MenuItemData food : menuList) {
                                System.out.println(food.toString());
                            }

                            while (true) {
                                boolean orderHandled = false;
                                System.out.print("Enter the Id of the food you want to order: ");
                                int userInput = scanner.nextInt();
                                scanner.nextLine();
                                for (OrderDetail food : orderDetail) {
                                    if (userInput == food.getFoodId()) {
                                        food.setQuantity(food.getQuantity() + 1);
                                        orderHandled = true;
                                    }
                                }
                                if (!orderHandled) {
                                    for (MenuItemsResponse.MenuItemData food : menuList) {
                                        if (userInput == food.getItemId()) {
                                            orderDetail.add(new OrderDetail(food.getItemId(), food.getPrice(), 1));
                                        }
                                    }
                                }
                                System.out.println("Order added to your cart!");
                                System.out.println("Current cart:");
                                for (OrderDetail food : orderDetail) {
                                    System.out.println("FoodID: " + food.getFoodId() + " Price: " + food.getPrice()
                                            + " Quantity: " + food.getQuantity());
                                }
                                System.out.println(
                                        "Enter 'Yes' to continue ordering, or 'No' to finish and send the order.");
                                if (scanner.nextLine().toLowerCase().equals("no")) {
                                    break;
                                }
                            }
                            ActorSystem system = getContext().getSystem();
                            ActorSelection orderSystem = system
                                    .actorSelection("akka.tcp://order-system@localhost:2553/user/order-service");
                            System.out.println("user make an order to order system");

                            Restaurant restaurant = new Restaurant(restaurantId, restaurantPhone,
                                    restaurantAddress);
                            ArrayList<Order> order = new ArrayList<>();
                            order.add(new Order(restaurant, orderDetail));
                            orderSystem.tell(new CustomerOrderMessage(
                                    new Customer(customerId, customerAddress, customerPhone), order), getSelf());
                        })
                .build();
    }

    private void requestPayment(OrderConfirmMessage msg) {
        double amountToPay = msg.getTotalPrice();
        int orderId = msg.getOrderId();

        System.out.print("Enter payment method (Card/Cash): ");
        String paymentMethod = scanner.nextLine().trim();

        // Validate input and default to Card if invalid
        if (!paymentMethod.equalsIgnoreCase("Card") && !paymentMethod.equalsIgnoreCase("Cash")) {
            System.out.println("Invalid input. Defaulting to Card.");
            paymentMethod = "Card";
        }

        if (paymentMethod.equalsIgnoreCase("Card")) {

            System.out.println("Checking saved card for Customer " + customerId + 1);
            String encryptedCardNumber = getEncryptedCardNumber(this.customerId);

            String cardNumber;

            try (Scanner scanner = new Scanner(System.in)) {
                if (encryptedCardNumber != null) {
                    // Decrypt the card number for display purposes
                    String decryptedCardNumber = decryptCardNumber(encryptedCardNumber);
                    System.out.println("Your saved card number is: " + maskedCardNumber(decryptedCardNumber));
                    System.out.print("Do you want to use this card? (yes/no): ");
                    String response = scanner.nextLine().trim();

                    if (response.equalsIgnoreCase("yes")) {
                        cardNumber = decryptedCardNumber;
                    } else {
                        System.out.print("Please enter your new card number: ");
                        cardNumber = scanner.nextLine().trim();
                        encryptedCardNumber = encryptCardNumber(cardNumber);
                        updateUserCardNumberInDatabase(this.customerId, encryptedCardNumber);
                    }
                } else {
                    System.out.print("Please enter your card number: ");
                    cardNumber = scanner.nextLine().trim();
                    encryptedCardNumber = encryptCardNumber(cardNumber);
                    updateUserCardNumberInDatabase(this.customerId, encryptedCardNumber);
                }
            }
        }

        OrderPaymentMessage paymentMessage = new OrderPaymentMessage(orderId, this.customerId, amountToPay,
                paymentMethod);
        paymentServiceActor.tell(paymentMessage, getSelf());
    }

    private String getEncryptedCardNumber(int customerId) {

        mongoClient = MongoClients.create(dbURL);
        database = mongoClient.getDatabase("foodie");
        collection = database.getCollection("users");

        System.out.println("Filter database for customerId " + customerId + 2);

        Bson filter = Filters.eq("customerId", customerId);
        System.out.println("Filtered " + filter + 3);

        Document userDocument = collection.find(filter).first();
        if (userDocument != null && userDocument.containsKey("cardNumber")) {
            String cardNumber = userDocument.getString("cardNumber");
            System.out.println("Filtered card number: " + cardNumber + 4);
            return cardNumber;
        } else {
            System.out.println("No filtered card number found" + 5);
            return null; // No card number found
        }

    }

    private String encryptCardNumber(String cardNumber) {
        // Implement encryption logic here
        // Example: use AES or similar encryption algorithm
        String encryptedCardNumber = cardNumber;
        return encryptedCardNumber; // Return the encrypted card number
    }

    private String decryptCardNumber(String encryptedCardNumber) {
        // Implement decryption logic here
        String decryptedCardNumber = encryptedCardNumber;
        return decryptedCardNumber; // Return the decrypted card number
    }

    private String maskedCardNumber(String cardNumber) {
        // Return a masked version of the card number, like "**** **** **** 1234"
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    private void updateUserCardNumberInDatabase(int customerId, String encryptedCardNumber) {
        Bson filter = Filters.eq("customerId", customerId);
        Bson updateOperation = Updates.set("cardNumber", encryptedCardNumber);
        collection.updateOne(filter, updateOperation);
        System.out.println(encryptedCardNumber + " is updated to database.");
    }
}