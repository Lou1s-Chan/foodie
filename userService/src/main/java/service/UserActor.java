package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

    private ActorRef paymentServiceActor;  

    public UserActor(ActorRef paymentServiceActor) {
        this.paymentServiceActor = paymentServiceActor;
    }

    public static Props props(ActorRef paymentServiceActor) {
        return Props.create(UserActor.class, () -> new UserActor(paymentServiceActor));
    }

    @Override
    public Receive createReceive() {
        return new ReceiveBuilder()
                .match(Customer.class,
                        msg -> {
                            customerId = msg.getCustomerId();
                            customerAddress = msg.getCustomerAddress();
                            customerPhone = msg.getCustomerPhone();
                        })
                .match(OrderConfirmMessage.class,
                        msg -> {
                            // Use the totalPrice from OrderConfirmMessage
                            double amountToPay = msg.getTotalPrice();
                            int orderId = msg.getOrderId();

                            System.out.print("Enter payment method (Card/Cash): ");
                            String paymentMethod = scanner.nextLine().trim();
                        
                            // Validate input and default to Card if invalid
                            if (!paymentMethod.equalsIgnoreCase("Card") && !paymentMethod.equalsIgnoreCase("Cash")) {
                                System.out.println("Invalid input. Defaulting to Card.");
                                paymentMethod = "Card";
                            }
                        
                            OrderPaymentMessage paymentMessage = new OrderPaymentMessage(orderId, amountToPay, paymentMethod);
                            paymentServiceActor.tell(paymentMessage, getSelf());
                        })
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
                            // OrderDetail[] orderDetail = { new OrderDetail(1, 18.88, 3), new
                            // OrderDetail(2, 28.88, 5) };
                            ArrayList<Order> order = new ArrayList<>();
                            order.add(new Order(restaurant, orderDetail));
                            orderSystem.tell(new CustomerOrderMessage(
                                    new Customer(customerId, customerAddress, customerPhone), order), getSelf());
                        })
                .build();
    }
}