package ie.foodie.services;

import scala.concurrent.duration.Duration;
import akka.actor.*;
import ie.foodie.actors.ActorAllocator;
import ie.foodie.messages.*;

import java.util.concurrent.TimeUnit;

public class DeliveryService extends AbstractActorWithTimers {

    private ActorSelection orderServiceActor;
    private ActorSelection driverServiceActor;

    public DeliveryService() {}

    @Override
    public void preStart() {
        ActorSystem system = getContext().getSystem();

        this.orderServiceActor = ActorAllocator.getOrderActor(system);
        this.driverServiceActor = ActorAllocator.getDriverActor(system);
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(OrderDeliveryMessage.class, this::processDelivery)
                .match(DriverService.InternalMsgNoDriver.class, this::processNoDriver)
                .match(DeliveryQueryMessage.class, this::dispatchMsgForwader)
                .match(DeliveryCompleteMessage.class, this::completeDelivery)
                .build();
    }

    private void completeDelivery(DeliveryCompleteMessage message){

        DriverService.InternalMsgFreeDriver msgFreeDriver = new DriverService.InternalMsgFreeDriver(
                message.getOrderId(), Integer.parseInt(message.getStatus()));
        driverServiceActor.tell(msgFreeDriver, getSelf());

        DeliveryCompleteMessage deliveryCompleteMessage = new DeliveryCompleteMessage(
                message.getOrderId(), "Delivered");

        orderServiceActor.tell(deliveryCompleteMessage, getSelf());

        System.out.println("Order: " + message.getOrderId() + " is delivered.\n");
    }
    
    private void dispatchMsgForwader (DeliveryQueryMessage message) {
        orderServiceActor.tell(message, getSelf());
    }

    private void processNoDriver(DriverService.InternalMsgNoDriver message) {
        System.out.println("No suitable driver matched the order: "+ message.getOrderId() +" at this time." + "\n"
                           + "But we will try our best to allocate one.\n");
        getContext().system().scheduler().scheduleOnce(
                Duration.create(60, TimeUnit.SECONDS),
                getSender(),
                new DriverService.InternalMsgSlaveDriver(message.getOrderId()),
                getContext().dispatcher(),
                getSelf()
        );

    }

    private void processDelivery(OrderDeliveryMessage message) {

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
