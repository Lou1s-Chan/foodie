package ie.foodie;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ie.foodie.services.DeliveryService;
import ie.foodie.services.DriverService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.ApplicationContext;
import ie.foodie.services.SSE.SSEController;

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
public class Main {
    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(Main.class, args);
        SSEController sseController = applicationContext.getBean(SSEController.class);

        ActorSystem system = ActorSystem.create("delivery-system");

        final Props DeliveryServiceProp = Props.create(DeliveryService.class, () -> new DeliveryService(sseController));
        final ActorRef deliveryActorRef
                = system.actorOf(DeliveryServiceProp, "delivery-service");

        final Props DriverServiceProp = Props.create(DriverService.class, () -> new DriverService(sseController));
        final ActorRef driverActorRef
                =system.actorOf(DriverServiceProp, "driver-service");

        System.out.println("Delivery Service (Driver Allocator) Starts.");
    }
}