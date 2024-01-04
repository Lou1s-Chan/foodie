package ie.foodie.actors;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

public class ActorProvider {

    public static ActorSelection getDeliveryActor(ActorSystem system) {
        String deliveryHost = "delivery-service";
        // akka.<protocol>://<actorsystemname>@<hostname>:<port>/<actor path>
        return system.actorSelection("akka.tcp://delivery-system@" + deliveryHost + ":2554/user/delivery-service");
    }

    public static ActorSelection getRestaurantActor(ActorSystem system) {
        String restaurantHost = "restaurant-service";
        // akka.<protocol>://<actorsystemname>@<hostname>:<port>/<actor path>
        return system.actorSelection("akka.tcp://restaurant-system@" + restaurantHost + ":2551/user/restaurant-service");
    }

    public static ActorSelection getPaymentActor(ActorSystem system) {
        String paymentHost = "payment-service";
        // akka.<protocol>://<actorsystemname>@<hostname>:<port>/<actor path>
        return system.actorSelection("akka.tcp://payment-system@" + paymentHost + ":2555/user/payment-service");
    }
}
