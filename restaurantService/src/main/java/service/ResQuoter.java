package service;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import ie.foodie.messages.RestaurantOrderMessage;

public class ResQuoter extends AbstractActor {

    @Override
    public Receive createReceive() {
        System.out.println("Restaurant Service listener has been set up");
        return new ReceiveBuilder().match(
                RestaurantOrderMessage.class, msg -> {
                    System.out.println("Received an order...");
                    System.out.println("Customer id: " + msg.getCustomerId());
                    System.out.println("Order: " + msg.getOrder());
                }
        ).build();
    }
}
