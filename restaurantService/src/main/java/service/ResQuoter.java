package service;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import ie.foodie.messages.RestaurantOrderMessage;
import ie.foodie.messages.models.Order;

public class ResQuoter extends AbstractActor {

    @Override
    public Receive createReceive() {
        System.out.println("\n***Restaurant Service listener has been set up***\n");
        return new ReceiveBuilder().match(
                RestaurantOrderMessage.class, msg -> {
                    System.out.println("Received an order...");
                    System.out.println("======================================");
                    System.out.println("Customer id: " + msg.getCustomerId());
                    System.out.println("Order details: " + "\n - Restaurant ID: " + (msg.getOrder().getRestaurant().getRestaurantId())
                                    + "\n - Restaurant Address: " + (msg.getOrder().getRestaurant().getRestaurantAddress())
                                    + "\n - Restaurant Phone: " + (msg.getOrder().getRestaurant().getRestaurantPhone())
                            );
                    System.out.println("Ordered Items:");
                    for (Order.OrderDetail item : msg.getOrder().getOrderDetails()) {
                        System.out.println(" - Food ID: " + item.getFoodId()
                                + ", Price: " + item.getPrice()
                                + ", Quantity: " + item.getQuantity());
                    }
                    getSender().tell("Order received", getSelf());
                }
        ).build();
    }
}
