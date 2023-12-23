package ie.foodie.services;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Field;
import com.mongodb.client.model.Filters;
import ie.foodie.messages.*;

import java.time.Duration;

import com.mongodb.client.*;
import com.mongodb.client.model.Sorts;

import org.bson.Document;

public class DeliveryService extends AbstractActor {
    private final ActorRef orderServiceActor;

    public DeliveryService(MongoClient mongoClient, MongoDatabase mongoDatabase) {
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoDatabase;
        this.orderServiceActor = null;
    }

    public DeliveryService(ActorRef orderServiceActor, MongoClient mongoClient, MongoDatabase mongoDatabase) {

        this.orderServiceActor = orderServiceActor;
        this.mongoClient = mongoClient;
        this.mongoDatabase = mongoDatabase;
    }

    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    private final String mgdbURL = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";

    public DeliveryService(String mgdbUrlArg, ActorRef orderServiceActor){
        this.orderServiceActor = orderServiceActor;
        String finalMgdbUrl = !mgdbUrlArg.isEmpty() ? mgdbUrlArg: this.mgdbURL;
        this.mongoClient = MongoClients.create(finalMgdbUrl);
        this.mongoDatabase = mongoClient.getDatabase("foodie");
        try {
            mongoDatabase.listCollectionNames().first();
            System.out.println("Contact Driver Database Successfully!");
        } catch (Exception e) { e.printStackTrace();}
    }

    public class driverMatcher{
        List<Field<?>> fields = Arrays.asList(
                new Field<>("match_score",
                        new Document("$multiply",
                                Arrays.asList("$location_parameter", "$rating")))
        );
        MongoCollection<Document> collection = mongoDatabase.getCollection("drivers");
        Document bestDriver = collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("availability", "free")),
                Aggregates.addFields(fields),
                Aggregates.sort(Sorts.ascending("match_score")),
                Aggregates.limit(1)
        )).first();
    }

    @Override
    public Receive createReceive(){
        return receiveBuilder()
                .match(OrderDeliveryMessage.class, this::orderDelivery)
//                .match(CheckDeliveryStatus.class, this::checkDeliveryStatus)
                .build();
    }

    private void orderDelivery(OrderDeliveryMessage message) {

        driverMatcher dMatcher = new driverMatcher();

        int orderId = message.getOrderId();
        String customerAddress = message.getCustomer().getCustomerAddress();
        String customerPhone = message.getCustomer().getCustomerPhone();
        String restaurantAddress = message.getOrder().getRestaurant().getRestaurantAddress();
        String restaurantPhone = message.getOrder().getRestaurant().getRestaurantPhone();

        if(dMatcher.bestDriver != null) {
            dMatcher.bestDriver.toJson();
            // Estimated Delivery Time is calculated by (rating * location_parameter * 3.75) seconds
            Double estTime = (dMatcher.bestDriver.getDouble("rating") *
                    dMatcher.bestDriver.getInteger("location_parameter")) * 3.75;
            Long estTimeLong = Math.round(estTime);
            LocalDateTime eTa = LocalDateTime.now().plusSeconds(estTimeLong);
            String eTaString = eTa.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

            String driverName = dMatcher.bestDriver.getString("name");
            String driverPhone = dMatcher.bestDriver.getString("phone");

            System.out.println("ORDER DISPATCHED" + "\n"
                    + "Order ID: " + orderId + "\n"
                    + "Customer Address: " + customerAddress + "\n"
                    + "Customer Phone: " + customerPhone + "\n"
                    + "Restaurant Address: " + restaurantAddress + "\n"
                    + "Restaurant Phone: " + restaurantPhone + "\n"
                    + "Driver Name: " + driverName +"\n"
                    + "Driver Phone: " + driverPhone + "\n"
                    + "Estimated Time Arrival: " + eTaString +"\n");

        } else {}
    }

//    private void checkDeliveryStatus(CheckDeliveryStatus message) {
//        final ActorRef sender = getSender();
//        executorService.submit(() -> {
//            TummySavior tummySavior = new TummySavior();
//            if(tummySavior.tummySaviorDelivered()) {
//                scheduler.cancel();
//                if(orderServiceActor != null) {
//                    OrderDeliveredMessage orderDeliveredMessage = new OrderDeliveredMessage(
//                            message.getOrderId(), "DELIVERED");
//                    orderServiceActor.tell(orderDeliveredMessage, getSelf());
//                }
//                OrderDeliveringMessage orderDeliveringMessage = new OrderDeliveringMessage(
//                        message.getOrderId(), "DELIVERED", "Order delivered");
//                sender.tell(orderDeliveringMessage, getSelf());
//            } else {
//                OrderDeliveringMessage orderDeliveringMessage = new OrderDeliveringMessage(
//                        message.getOrderId(), "UNDELIVERED", "Food undelivered yet");
//                sender.tell(orderDeliveringMessage, getSelf());
//            }
//        });
//    }

//    private static class CheckDeliveryStatus{
//        private final OrderDeliveryMessage orderDeliveryMessage;
//        CheckDeliveryStatus(OrderDeliveryMessage message) {
//            this.orderDeliveryMessage = message;
//        }
//
//        public int getOrderId() {
//            return orderDeliveryMessage.getOrderId();
//        }
//    }

}
