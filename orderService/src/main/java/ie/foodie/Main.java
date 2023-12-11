package ie.foodie;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import ie.foodie.services.OrderService;

public class Main {
    public static void main(String[] args) {
        Config config = ConfigFactory.load();
        ActorSystem system = ActorSystem.create("order-system", config);

        ActorRef orderActorRef
                = system.actorOf(Props.create(OrderService.class), "order-service");

        System.out.println("Order service starts");
//        orderActorRef.tell(customerOrderMessage, ActorRef.noSender());
    }
}