package service;

import actors.ActorProvider;
import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import ie.foodie.messages.*;

public class PaymentService extends AbstractActor {

    ActorSystem system = getContext().getSystem();
    ActorSelection orderServiceActor = ActorProvider.getOrderServiceActor(system);
    ActorSelection userActor = ActorProvider.getUserActor(system);

    // Default constructor for cases where OrderService actor is not needed initially
    public PaymentService() {
        this.orderServiceActor = null;
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

        System.out.println("Processing payment for Order ID: " + orderId + 
                           ", Total Price: " + totalPrice + 
                           ", Payment Method: " + paymentMethod);

        boolean isPaymentSuccessful = processWithPaymentGateway(message);
        ActorRef sender = getSender(); // Storing the reference to the sender (UserActor)
    
        if (isPaymentSuccessful) {
            // Send confirmation to OrderService
            if (orderServiceActor != null) {
                PaymentConfirmMessage confirmMessage = new PaymentConfirmMessage(message.getOrderId(), "SUCCESS");
                orderServiceActor.tell(confirmMessage, getSelf());
            }
    
            // Send success message to UserActor
            PaymentStatusMessage statusMessage = new PaymentStatusMessage(message.getOrderId(), "SUCCESS", "Payment processed successfully.");
            sender.tell(statusMessage, getSelf());
        } else {

            // Handle payment failure scenario
            PaymentStatusMessage statusMessage = new PaymentStatusMessage(message.getOrderId(), "FAILURE", "Payment processing failed.");
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
