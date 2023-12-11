package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import ie.foodie.messages.MenuItemsResponse;
import ie.foodie.messages.OrderConfirmMessage;
import ie.foodie.messages.RestaurantQueryMessage;
import ie.foodie.messages.RestaurantsResponse;
import ie.foodie.messages.RestaurantsResponse.RestaurantData;
import ie.foodie.messages.models.Order.Restaurant;

public class UserActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        System.out.println("**User service listener has been set up**\n");
        return new ReceiveBuilder()
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
                                    int restaurantId = scanner.nextInt();
                                    if ((restaurantId > 0) && (restaurantId < 31)) {
                                        getSender().tell(
                                                new RestaurantQueryMessage(
                                                        RestaurantQueryMessage.QueryType.MENU_REQUEST,
                                                        restaurantId),
                                                getSelf());
                                        System.out.println("Menu request sent!");
                                        scanner.close();
                                        break; // Exit the loop if a valid integer is entered
                                    }
                                } else {
                                    System.out.println("Invalid input. Please enter a valid integer.");
                                    scanner.nextLine(); // Consume the invalid input to avoid an infinite loop
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
                        })
                .build();
    }
}