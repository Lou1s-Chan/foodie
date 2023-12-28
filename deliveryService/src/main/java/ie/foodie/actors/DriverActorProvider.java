package ie.foodie.actors;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;

public class DriverActorProvider {
    public static ActorSelection getDriverActor(ActorSystem system) {
        return system.actorSelection("/user/driver-service");
    }
}
