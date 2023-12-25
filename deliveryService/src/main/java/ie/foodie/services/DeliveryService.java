package ie.foodie.services;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import ie.foodie.messages.*;
import org.bson.Document;

public class DeliveryService extends AbstractActorWithTimers {
    private ActorRef orderServiceActor;
    private TummySavior tummySavior = new TummySavior();
    public DeliveryService() {this.orderServiceActor = null;}

    public DeliveryService(ActorRef orderServiceActor){
        this.orderServiceActor = orderServiceActor;
        this.tummySavior = new TummySavior();
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(OrderDeliveryMessage.class, this::processDelivery)
                .match(CompleteDelivery.class, this::completeDelivery)
                .build();
    }

    @Override
    public void postStop() {
        tummySavior.closeDbConnection();
    }

    private void completeDelivery(CompleteDelivery message){
        tummySavior.FreeDriver(message.getOrderId(), message.getDriverId());
        System.out.println("Order: " + message.getOrderId() + " is delivered.");
        DeliveryCompleteMessage deliveryCompleteMessage = new DeliveryCompleteMessage(
                message.getOrderId(), "DELIVERED");
        orderServiceActor.tell(deliveryCompleteMessage, getSelf());
    }

    private void processDelivery(OrderDeliveryMessage message) {
        orderServiceActor = getSender();
        Document bestDriver = tummySavior.findBestDriver();

        int orderId = message.getOrderId();
        String customerAddress = message.getCustomer().getCustomerAddress();
        String customerPhone = message.getCustomer().getCustomerPhone();
        String restaurantAddress = message.getOrder().getRestaurant().getRestaurantAddress();
        String restaurantPhone = message.getOrder().getRestaurant().getRestaurantPhone();

        bestDriver.toJson();

        int driverId = bestDriver.getInteger("driver_id");
        String driverName = bestDriver.getString("name");
        int driverPhone = bestDriver.getInteger("phone");

        // Estimated Delivery Time is calculated by (rating * location_parameter * 3.75) seconds
        double estTime = (bestDriver.getDouble("rating") *
                          bestDriver.getDouble("location_parameter")) * 3.75;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        LocalDateTime dispatchedTime = LocalDateTime.now();
        long estTimeLong = Math.round(estTime);
        LocalDateTime eta = dispatchedTime.plusSeconds(estTimeLong);
        String eTa = eta.format(formatter);

        System.out.println("ORDER DISPATCHED" + "\n"
                    + "Order ID: " + orderId + "\n"
                    + "Customer Address: " + customerAddress + "\n"
                    + "Customer Phone: " + customerPhone + "\n"
                    + "Restaurant Address: " + restaurantAddress + "\n"
                    + "Restaurant Phone: " + restaurantPhone + "\n"
                    + "Driver Name: " + driverName +"\n"
                    + "Driver Phone: " + driverPhone + "\n"
                    + "Estimated Time Arrival: " + eTa +"\n");

        DeliveryQueryMessage deliveryQueryMessage = new DeliveryQueryMessage(
                message.getOrderId(), "DISPATCHED", "Order is on its way.");
        orderServiceActor.tell(deliveryQueryMessage, getSelf());

        tummySavior.SlaveDriver(orderId, driverId);

        System.out.println("Timer is tick tick, wait for the driver delivering the order: " + orderId + ".");
        String DELIVERY_TIMER = "DELIVERY-TIMER-" + orderId;
        getTimers().startSingleTimer(DELIVERY_TIMER,
                new CompleteDelivery(orderId, driverId),
                Duration.ofSeconds(estTimeLong));
    }

    public static class CompleteDelivery {
        private final int orderId;
        private final int driverId;

        public CompleteDelivery(int orderId, int driverId) {
            this.orderId = orderId;
            this.driverId = driverId;
        }

        public int getDriverId() {
            return driverId;
        }

        public int getOrderId() {
            return orderId;
        }
    }
}
