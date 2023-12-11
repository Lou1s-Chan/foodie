package service;

import java.util.List;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import ie.foodie.messages.OrderConfirmMessage;
import ie.foodie.messages.RestaurantsResponse;
import ie.foodie.messages.RestaurantsResponse.RestaurantData;
import ie.foodie.messages.models.Order.Restaurant;

public class UserActor extends AbstractActor {

    @Override
    public Receive createReceive() {
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
                            List<RestaurantData> restaurantList = msg.getRestaurants();
                            for (RestaurantData restaurant : restaurantList) {
                                System.out.println(restaurant.toString());
                            }
                        })
                .build();
    }
}