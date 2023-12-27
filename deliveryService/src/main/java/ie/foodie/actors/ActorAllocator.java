package ie.foodie.actors;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

public class ActorAllocator {
    public static ActorSelection getDriverActor(ActorSystem system) {
        return system.actorSelection("/user/driver-service");
    }

    public static ActorSelection getDeliveryActor(ActorSystem system) {
        return system.actorSelection("/user/delivery-service");
    }

    public static ActorSelection getOrderActor(ActorSystem system) {
        String orderHost = "localhost";
        return system.actorSelection("akka.tcp://order-system@" + orderHost + ":2553/user/order-service");
    }
}
