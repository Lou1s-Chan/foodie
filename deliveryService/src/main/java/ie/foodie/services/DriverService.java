package ie.foodie.services;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import ie.foodie.actors.FoodieActor;
import ie.foodie.messages.*;
import org.bson.Document;
import scala.concurrent.duration.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class DriverService extends FoodieActor {
    private TummySavior tummySavior = new TummySavior();

    public DriverService() {}

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(InternalMsgSlaveDriver.class, this::startDelivery)
                .match(InternalMsgFreeDriver.class, this::endDelivery)
                .build();
    }

    @Override
    public void postStop() {
        tummySavior.closeDbConnection();
    }

    public static class InternalMsgSlaveDriver {
        private final int orderId;
        private ActorRef userRef;

        public InternalMsgSlaveDriver(int orderId, ActorRef userRef) {
            this.orderId = orderId;
            this.userRef = userRef;
        }

        public int getOrderId() {
            return orderId;
        }

        public ActorRef getUserRef() {
            return userRef;
        }
    }

    public static class InternalMsgFreeDriver {
        private final int orderId;
        private final int driverId;
        private ActorRef userRef;

        public InternalMsgFreeDriver(int orderId, int driverId, ActorRef userRef) {
            this.orderId = orderId;
            this.driverId = driverId;
            this.userRef = userRef;
        }

        public int getOrderId() {
            return orderId;
        }

        public int getDriverId() {
            return driverId;
        }

        public ActorRef getUserRef() {
            return userRef;
        }
    }

    private void startDelivery(InternalMsgSlaveDriver msgSlaveDriver) {

        Document bestDriver = tummySavior.BestDriver();
        if (bestDriver != null) {
            LocalDateTime dispatchedTime = LocalDateTime.now();

            Integer driverId = bestDriver.getInteger("driver_id");
            String driverName = bestDriver.getString("name");
            int driverPhone = bestDriver.getInteger("phone");

            // Estimated Delivery Time is calculated by (rating * location_parameter * 3.75) seconds
            double estTime = (bestDriver.getDouble("rating") *
                    bestDriver.getDouble("location_parameter")) * 3.75;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
            long estTimeLong = Math.round(estTime);
            LocalDateTime eta = dispatchedTime.plusSeconds(estTimeLong);
            String eTa = eta.format(formatter);

            tummySavior.SlaveDriver(msgSlaveDriver.getOrderId(), driverId);
            System.out.println("ORDER DISPATCHED" + "\n"
                    + "Order ID: " + msgSlaveDriver.getOrderId() + "\n"
                    + "Driver Name: " + driverName +"\n"
                    + "Driver Phone: " + driverPhone + "\n"
                    + "Estimated Time Arrival: " + eTa +"\n");

            DeliveryQueryMessage deliveryQueryMessage = new DeliveryQueryMessage(msgSlaveDriver.getOrderId(),
                    "Dispatched", "Your order is on its way, the ETA is "+ eTa +"\n"
                                                            + "Your driver is " + driverName + ".\n"
            ,msgSlaveDriver.getUserRef());
            getSender().tell(deliveryQueryMessage, getSelf());

            // Simulate the delivery by setting a timer
            getContext().system().scheduler().scheduleOnce(
                    Duration.create(estTimeLong, TimeUnit.SECONDS),
                    getSender(),
                    new DeliveryQueryMessage(msgSlaveDriver.getOrderId(), "Delivered", driverId.toString(),
                            msgSlaveDriver.getUserRef()),
                    getContext().dispatcher(),
                    getSelf()
            );
        } else {
            DeliveryQueryMessage deliveryQueryMessage = new DeliveryQueryMessage(msgSlaveDriver.getOrderId(),
                    "NoDriver", "No available driver at this time.", msgSlaveDriver.getUserRef());
            getSender().tell(deliveryQueryMessage, getSelf());
        }
    }

    private void endDelivery(InternalMsgFreeDriver msgFreeDriver) {
        tummySavior.FreeDriver(msgFreeDriver.getOrderId(), msgFreeDriver.getDriverId());
    }
}
