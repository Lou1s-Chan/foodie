package ie.foodie.services;

import akka.actor.AbstractActor;
import ie.foodie.messages.CustomerOrderMessage;
import ie.foodie.messages.OrderConfirmMessage;
import ie.foodie.messages.PaymentConfirmMessage;

public class OrderService extends AbstractActor {

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CustomerOrderMessage.class, msg -> {
                    System.out.println("received order message " + msg.getCustomer().getCustomerId());
                    // calculate price and store to db

                    OrderConfirmMessage orderConfirmMessage = new OrderConfirmMessage(1, 5);
                    getSender().tell(orderConfirmMessage, getSelf());
                })
                .match(PaymentConfirmMessage.class, msg -> {
                    // change status in db
                })
                .build();
    }


}
