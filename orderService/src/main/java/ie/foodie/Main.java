package ie.foodie;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ie.foodie.database.OrderDao;
import ie.foodie.messages.OrderConfirmMessage;
import ie.foodie.services.OrderService;

public class Main {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("test-system");

        ActorRef orderActorRef
                = system.actorOf(Props.create(OrderService.class), "order-service");

        OrderDao.connect();

        orderActorRef.tell(new OrderConfirmMessage(1, 5), ActorRef.noSender());
        System.out.println("Hello world!");
    }
}