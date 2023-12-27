package service;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import service.SSE.SSEController;


@SpringBootApplication
public class Main {
    static ActorSystem system = ActorSystem.create("restaurant-system");

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Main.class, args);
        SSEController sseController = applicationContext.getBean(SSEController.class);
        final Props ResServiceProp = Props.create(ResActor.class, () -> new ResActor(sseController));
        final ActorRef ResServiceRef = system.actorOf(ResServiceProp, "restaurant-service");
        System.out.println("Running Restaurant Service");


        try {
            String OrderPath = "akka.tcp://order-system@order-host:2553/user/order-service";
            ActorSelection orderActor = system.actorSelection(OrderPath);
            System.out.println("remote order Actor: " + orderActor);

            String userPath = "akka.tcp://user-system@localhost:2552/user/user-service";
            ActorSelection userActor = system.actorSelection(userPath);
            System.out.println("remote user Actor: " + userActor);
        } catch (Exception e) {
            System.out.println("Error connecting to order service.");
            e.printStackTrace();
        }
    }
}
