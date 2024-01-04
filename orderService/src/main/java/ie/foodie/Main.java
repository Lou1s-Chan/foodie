package ie.foodie;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ie.foodie.SSE.SSEController;
import ie.foodie.services.OrderService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Main.class, args);
        SSEController sseController = applicationContext.getBean(SSEController.class);
        ActorSystem system = ActorSystem.create("order-system");

        ActorRef orderActorRef
                = system.actorOf(Props.create(OrderService.class, () -> new OrderService(sseController)), "order-service");

        System.out.println("Order service starts");
//        orderActorRef.tell(customerOrderMessage, ActorRef.noSender());
    }
}