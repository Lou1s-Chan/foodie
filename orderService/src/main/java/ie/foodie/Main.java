package ie.foodie;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ie.foodie.services.OrderService;

public class Main {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("order-system");

        ActorRef orderActorRef
                = system.actorOf(Props.create(OrderService.class), "order-service");

        System.out.println("Order service starts");
//        orderActorRef.tell(customerOrderMessage, ActorRef.noSender());
    }
}