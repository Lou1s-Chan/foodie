package ie.foodie.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

public class ActorProvider {

    public static ActorSelection getDeliveryActor(ActorSystem system) {
        String deliveryHost = "localhost";
        // akka.<protocol>://<actorsystemname>@<hostname>:<port>/<actor path>
        return system.actorSelection("akka.tcp://delivery-system@" + deliveryHost + ":2554/user/delivery-service");
    }

    public static ActorSelection getRestaurantActor(ActorSystem system) {
        String restaurantHost = "localhost";
        // akka.<protocol>://<actorsystemname>@<hostname>:<port>/<actor path>
        return system.actorSelection("akka.tcp://restaurant-ref@" + restaurantHost + ":2551/user/restaurant-system");
    }
}
