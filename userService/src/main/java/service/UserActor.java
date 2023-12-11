package service;

import java.util.ArrayList;
import java.util.List;

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