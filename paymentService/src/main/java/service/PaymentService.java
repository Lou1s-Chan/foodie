package service;

import actors.ActorProvider;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import ie.foodie.messages.*;

public class PaymentService extends AbstractActor {

    private ActorSelection orderServiceActor;
    private ActorSelection userActor;

    public PaymentService() {
        // Empty constructor
    }

    @Override
    public void preStart() {
        ActorSystem system = getContext().getSystem();
        this.orderServiceActor = ActorProvider.getOrderServiceActor(system);
        this.userActor = ActorProvider.getUserActor(system);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(OrderPaymentMessage.class, this::processPayment)
                .build();
    }

    private void processPayment(OrderPaymentMessage message) {


        // Process the payment
        int orderId = message.getOrderId();
        double totalPrice = message.getTotalPrice();
        String paymentMethod = message.getPaymentMethod();
        ActorRef sender = getSender(); // Storing the reference to the sender (UserActor)

        System.out.println("Processing payment for Order ID: " + orderId + 
                           ", Total Price: " + totalPrice + 
                           ", Payment Method: " + paymentMethod);

        if (paymentMethod == "Card"){
            boolean isPaymentSuccessful = processWithPaymentGateway(message);
        
            if (isPaymentSuccessful) {
                // Send success message to OrderService
                PaymentStatusMessage statusMessage = new PaymentStatusMessage(message.getOrderId(), "CARD-PAID", "Payment processed successfully.");
                
                if (orderServiceActor != null) {
                    orderServiceActor.tell(statusMessage, getSelf());
                    System.out.println("Confirmation sent to Order Service.");
                } else {                
                    System.out.println("Could not find Order Service.");
                }
        
                // Send success message to UserActor
                sender.tell(statusMessage, getSelf());
            } else {

                // Handle payment failure scenario
                PaymentStatusMessage statusMessage = new PaymentStatusMessage(message.getOrderId(), "CARD-UNPAID", "Payment processing failed.");
                sender.tell(statusMessage, getSelf());
            }
        } else {
            PaymentStatusMessage statusMessage = new PaymentStatusMessage(message.getOrderId(), "CASH-UNPAID", "Driver will collect cash.");
            if (orderServiceActor != null) {
                orderServiceActor.tell(statusMessage, getSelf());
                System.out.println("Confirmation sent to Order Service.");
            } else {                
                System.out.println("Could not find Order Service.");
            }
            sender.tell(statusMessage, getSelf());
        }
    }

    private boolean processWithPaymentGateway(OrderPaymentMessage message) {
        // Implement actual payment processing with Stripe or another gateway
        // This should include API calls to Stripe and handling responses
        // For now, let's assume the payment is always successful
        return true; // Placeholder for actual implementation
    }

    // Additional methods and functionality as needed
}
