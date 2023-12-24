package actors;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

public class ActorProvider {

    public static ActorSelection getOrderServiceActor(ActorSystem system) {
        String orderServiceHost = "localhost"; 
        int orderServicePort = 2553; 
        return system.actorSelection("akka.tcp://order-system@" + orderServiceHost + ":" + orderServicePort + "/user/order-service");
    }

    public static ActorSelection getUserActor(ActorSystem system) {
        String userServiceHost = "localhost"; // Update with actual host if different
        int userServicePort = 2552; // Update with actual port if different
        return system.actorSelection("akka.tcp://user-system@" + userServiceHost + ":" + userServicePort + "/user/user-service");
    }
}