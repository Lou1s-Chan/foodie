package ie.foodie;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ie.foodie.services.DeliveryService;

public class Main {
    static ActorSystem system = ActorSystem.create("delivery-system");
    public static void main(String[] args) {
        final Props DeliveryServiceProp = Props.create(DeliveryService.class);
        final ActorRef deliveryServiceRef
                = system.actorOf(DeliveryServiceProp, "delivery-service");
        System.out.println("Delivery service starts.");
    }
}