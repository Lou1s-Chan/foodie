import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import service.PaymentService; 

public class Main {
    public static void main(String[] args) {
        
        ActorSystem system = ActorSystem.create("payment-system");
        ActorRef paymentActorRef = system.actorOf(Props.create(PaymentService.class), "payment-service");
        System.out.println("Payment service starts");
        
    }
}