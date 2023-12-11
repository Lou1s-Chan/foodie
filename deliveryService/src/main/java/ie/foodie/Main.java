package ie.foodie;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ie.foodie.services.DeliveryService;

public class Main {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("delivery-system");

        ActorRef deliveryActorRef
                = system.actorOf(Props.create(DeliveryService.class), "delivery-service");

        System.out.println("Delivery service starts");
    }
}