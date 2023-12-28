package ie.foodie.services;

import ie.foodie.actors.FoodieActor;
import scala.concurrent.duration.Duration;
import akka.actor.*;
import ie.foodie.actors.DriverActorProvider;
import ie.foodie.messages.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class DeliveryService extends FoodieActor {

    private ActorSelection driverServiceActor;
    private ActorRef orderServiceActor = null;

    public DeliveryService() {}

    @Override
    public void preStart() {
        ActorSystem system = getContext().getSystem();
        this.driverServiceActor = DriverActorProvider.getDriverActor(system);
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(OrderDeliveryMessage.class, this::processDelivery)
                .match(DeliveryQueryMessage.class, this::msgForwarder)
                .build();
    }

    private void msgForwarder(DeliveryQueryMessage message) {
        switch (message.getStatus()) {
            case "Dispatched" :
                orderServiceActor.tell(message, getSelf());
                break;

            case "NoDriver" :
                System.out.println("No available driver matched the order: "+ message.getOrderId() +" at this time." + "\n"
                        + "But we will try our best to allocate one.\n");
                getContext().system().scheduler().scheduleOnce(
                        Duration.create(60, TimeUnit.SECONDS),
                        getSender(),
                        new DriverService.InternalMsgSlaveDriver(message.getOrderId()),
                        getContext().dispatcher(),
                        getSelf()
                );
                orderServiceActor.tell(message, getSelf());
                break;

            case "Delivered" :
                DriverService.InternalMsgFreeDriver msgFreeDriver = new DriverService.InternalMsgFreeDriver(
                        message.getOrderId(), Integer.parseInt(message.getMessage()));
                driverServiceActor.tell(msgFreeDriver, getSelf());

                DeliveryQueryMessage deliveryQueryMessage = new DeliveryQueryMessage(
                        message.getOrderId(), "Delivered",
                        "Your order " + message.getOrderId()
                                + " is delivered at "
                                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "."
                );
                orderServiceActor.tell(deliveryQueryMessage, getSelf());
                System.out.println("Order: " + message.getOrderId() + " is delivered.\n");
                break;
        }

    }

    private void processDelivery(OrderDeliveryMessage message) {
        this.orderServiceActor = getSender();

        int orderId = message.getOrderId();
        String customerAddress = message.getCustomer().getCustomerAddress();
        String customerPhone = message.getCustomer().getCustomerPhone();
        String restaurantAddress = message.getOrder().getRestaurant().getRestaurantAddress();
        String restaurantPhone = message.getOrder().getRestaurant().getRestaurantPhone();

        System.out.println("NEW DELIVER TASK CREATED" + "\n"
                    + "Order ID: " + orderId + "\n"
                    + "Customer Address: " + customerAddress + "\n"
                    + "Customer Phone: " + customerPhone + "\n"
                    + "Restaurant Address: " + restaurantAddress + "\n"
                    + "Restaurant Phone: " + restaurantPhone + "\n");
        System.out.println("We are allocating suitable driver at this time.\n");
        DriverService.InternalMsgSlaveDriver internalMsgSlaveDriver = new DriverService.InternalMsgSlaveDriver(
                message.getOrderId());
        driverServiceActor.tell(internalMsgSlaveDriver, getSelf());

        DeliveryQueryMessage deliveryQueryMessage = new DeliveryQueryMessage(orderId,
                "Pending", "Allocating suitable driver.");
        orderServiceActor.tell(deliveryQueryMessage, getSelf());
    }
}
