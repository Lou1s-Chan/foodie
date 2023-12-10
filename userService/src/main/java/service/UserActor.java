package service;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import service.message.OrderConfirmMessage;

public class UserActor extends AbstractActor {

    @Override
    public Receive createReceive() {
        return new ReceiveBuilder()
                .match(OrderConfirmMessage.class,
                        msg -> {
                            // message to payment
                            // foodOrder = msg.getInfo();
                            // paymentActorRef.tell(new PaymentMessage(paymentDetail, foodOrder), getSelf());
                        })
                .build();
    }
}