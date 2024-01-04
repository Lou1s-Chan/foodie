package actors;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

public class ActorProvider {

    public static ActorSelection getOrderServiceActor(ActorSystem system) {
        String orderServiceHost = "order-service";
        int orderServicePort = 2553; 
        return system.actorSelection("akka.tcp://order-system@" + orderServiceHost + ":" + orderServicePort + "/user/order-service");
    }
//
//    public static ActorSelection getUserActor(ActorSystem system) {
//        String userServiceHost = "localhost";
//        int userServicePort = 2552;
//        return system.actorSelection("akka.tcp://user-system@" + userServiceHost + ":" + userServicePort + "/user/user-service");
//    }
}

