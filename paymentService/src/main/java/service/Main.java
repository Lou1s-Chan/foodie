package service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import service.SSE.SSEController;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Main.class, args);
        SSEController sseController = applicationContext.getBean(SSEController.class);
        
        ActorSystem system = ActorSystem.create("payment-system");
        ActorRef paymentActorRef = system.actorOf(Props.create(PaymentService.class, ()-> new PaymentService(sseController)), "payment-service");
        System.out.println("Payment service starts");
        
    }
}