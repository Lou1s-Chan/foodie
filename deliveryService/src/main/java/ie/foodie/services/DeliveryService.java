package ie.foodie.services;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import ie.foodie.messages.*;

import java.time.Duration;

public class DeliveryService extends AbstractActor {
    private final ActorRef orderServiceActor;
    private Cancellable scheduler;
    private ExecutorService executorService;

    public DeliveryService() {
        this.orderServiceActor = null;
    }

    public DeliveryService(ActorRef orderServiceActor) {

        this.orderServiceActor = orderServiceActor;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(OrderDeliveryMessage.class, this::orderDelivery)
                .match(CheckDeliveryStatus.class, this::checkDeliveryStatus)
                .build();
    }

    private void orderDelivery(OrderDeliveryMessage message) {
        scheduler = getContext().system().scheduler().schedule(
                Duration.ofMillis(0),
                Duration.ofSeconds(5),
                getSelf(),
                new CheckDeliveryStatus(message),
                getContext().dispatcher(),
                getSelf()
        );

        int orderId = message.getOrderId();
        String customerAddress = message.getCustomer().getCustomerAddress();
        String customerPhone = message.getCustomer().getCustomerPhone();
        String restaurantAddress = message.getOrder().getRestaurant().getRestaurantAddress();
        String restaurantPhone = message.getOrder().getRestaurant().getRestaurantPhone();

        System.out.println("ORDER DISPATCHED" + "\n"
                           + "Order ID: " + orderId + "\n"
                           + "Customer Address: " + customerAddress + "\n"
                           + "Customer Phone: " + customerPhone + "\n"
                           + "Restaurant Address: " + restaurantAddress + "\n"
                           + "Restaurant Phone: " + restaurantPhone + "\n");
    }

    private void checkDeliveryStatus(CheckDeliveryStatus message) {
        final ActorRef sender = getSender();
        executorService.submit(() -> {
            TummySavior tummySavior = new TummySavior();
            if(tummySavior.tummySaviorDelivered()) {
                scheduler.cancel();
                if(orderServiceActor != null) {
                    OrderDeliveredMessage orderDeliveredMessage = new OrderDeliveredMessage(
                            message.getOrderId(), "DELIVERED");
                    orderServiceActor.tell(orderDeliveredMessage, getSelf());
                }
                OrderDeliveringMessage orderDeliveringMessage = new OrderDeliveringMessage(
                        message.getOrderId(), "DELIVERED", "Order delivered");
                sender.tell(orderDeliveringMessage, getSelf());
            } else {
                OrderDeliveringMessage orderDeliveringMessage = new OrderDeliveringMessage(
                        message.getOrderId(), "UNDELIVERED", "Food undelivered yet");
                sender.tell(orderDeliveringMessage, getSelf());
            }
        });
    }

    @Override
    public void postStop() {
        if (scheduler != null) {
            scheduler.cancel();
        }
        executorService.shutdown();
    }

    private static class CheckDeliveryStatus{
        private final OrderDeliveryMessage orderDeliveryMessage;
        CheckDeliveryStatus(OrderDeliveryMessage message) {
            this.orderDeliveryMessage = message;
        }

        public int getOrderId() {
            return orderDeliveryMessage.getOrderId();
        }
    }

}
