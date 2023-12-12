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

    @Override
    public Receive createReceive() {
        System.out.println("**User service listener has been set up**\n");
        return new ReceiveBuilder()
                .match(Customer.class,
                        msg -> {
                            customerId = msg.getCustomerId();
                            customerAddress = msg.getCustomerAddress();
                            customerPhone = msg.getCustomerPhone();
                        })
                .match(OrderConfirmMessage.class,
                        msg -> {
                            // message to payment
                            // foodOrder = msg.getInfo();
                            // paymentActorRef.tell(new PaymentMessage(paymentDetail, foodOrder),
                            // getSelf());
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
                                Scanner scanner = new Scanner(System.in);
                                // Check if the next input is an integer
                                if (scanner.hasNextInt()) {
                                    int selectedRestaurantId = scanner.nextInt();
                                    if ((selectedRestaurantId > 0) && (selectedRestaurantId < 31)) {
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
                                        scanner.close();
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
                            ActorSystem system = getContext().getSystem();
                            ActorSelection orderSystem = system
                                    .actorSelection("akka.tcp://order-system@localhost:2553/user/order-service");
                            System.out.println("user make an order to order system");

                            Restaurant restaurant = new Restaurant(restaurantId, restaurantPhone, restaurantAddress);
                            OrderDetail[] orderDetail = { new OrderDetail(1, 18.88, 3), new OrderDetail(2, 28.88, 5) };
                            Order[] order = { new Order(restaurant, orderDetail) };
                            orderSystem.tell(new CustomerOrderMessage(
                                    new Customer(customerId, customerAddress, customerPhone), order), getSelf());
                        })
                .build();
    }
}