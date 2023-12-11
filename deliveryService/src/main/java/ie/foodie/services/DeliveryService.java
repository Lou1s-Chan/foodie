package ie.foodie.services;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import ie.foodie.messages.*;

public class DeliveryService extends AbstractActor {
    private final ActorRef orderServiceActor;

    public DeliveryService() {
        this.orderServiceActor = null;
    }

    public DeliveryService(ActorRef orderServiceActor) {
        this.orderServiceActor = orderServiceActor;
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(OrderDeliveryMessage.class, this::orderDelivery)
                .build();
    }

    private void orderDelivery(OrderDeliveryMessage messageODM) {
        TummySavior tummySavior = new TummySavior();
        int orderId = messageODM.getOrderId();
        String customerAddress = messageODM.getCustomer().getCustomerAddress();
        String customerPhone = messageODM.getCustomer().getCustomerPhone();
        String restaurantAddress = messageODM.getOrder().getRestaurant().getRestaurantAddress();
        String restaurantPhone = messageODM.getOrder().getRestaurant().getRestaurantPhone();

        System.out.println("ORDER DISPATCHED" + "\n"
                           + "Order ID: " + orderId + "\n"
                           + "Customer Address: " + customerAddress + "\n"
                           + "Customer Phone: " + customerPhone + "\n"
                           + "Restaurant Address: " + restaurantAddress + "\n"
                           + "Restaurant Phone: " + restaurantPhone + "\n");

        boolean deliveryStatus = tummySavior.tummySaviorDelivered();
        ActorRef sender = getSender();

        if (deliveryStatus) {
            if (orderServiceActor != null){
                OrderDeliveredMessage orderDeliveredMessage = new OrderDeliveredMessage(
                        messageODM.getOrderId(), "DELIVERED");
                orderServiceActor.tell(orderDeliveredMessage, getSelf());
            }
            OrderDeliveringMessage orderDeliveringMessage = new OrderDeliveringMessage(
                    messageODM.getOrderId(), "DELIVERED", "Order delivered");
            sender.tell(orderDeliveringMessage, getSelf());
        } else {
            OrderDeliveringMessage orderDeliveringMessage = new OrderDeliveringMessage(
                    messageODM.getOrderId(), "UNDELIVERED", "Food undelivered yet");
            sender.tell(orderDeliveringMessage, getSelf());
        }
    }
}
