package ie.foodie;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.sun.org.apache.xpath.internal.operations.Or;
import ie.foodie.actors.ActorProvider;
import ie.foodie.database.OrderDao;
import ie.foodie.messages.OrderConfirmMessage;
import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;
import ie.foodie.services.OrderService;
import ie.foodie.messages.CustomerOrderMessage;

public class Main {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("order-system");

        ActorRef orderActorRef
                = system.actorOf(Props.create(OrderService.class), "order-service");

//        orderActorRef.tell(customerOrderMessage, ActorRef.noSender());
    }
}