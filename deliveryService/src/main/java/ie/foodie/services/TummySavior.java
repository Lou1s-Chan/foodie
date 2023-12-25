package ie.foodie.services;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TummySavior {
    private final MongoClient mongoClient;
    private final MongoDatabase mongoDatabase;
    public TummySavior() {
        String mgdbUrl = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";
        this.mongoClient = MongoClients.create(mgdbUrl);
        this.mongoDatabase = mongoClient.getDatabase("foodie");
    }
    public void closeDbConnection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public void SlaveDriver(int orderId, int driverId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String operationTime = LocalDateTime.now().format(formatter);
        MongoCollection<Document> collection = mongoDatabase.getCollection("delivery_tracking");
        Document newRecord = new Document("order_id", orderId)
                .append("dispatched_time", operationTime)
                .append("delivered_time", "undelivered yet")
                .append("by_driver", driverId)
                .append("status", "delivering");
        collection.insertOne(newRecord);

        // Select from driver database
        MongoCollection<Document> collection1 = mongoDatabase.getCollection("drivers");

        //Select from test driver database
        // MongoCollection<Document> collection1 = mongoDatabase.getCollection("test_drivers");
        collection1.updateOne(Filters.eq("driver_id", driverId), Updates.set("availability", "on_duty"));
    }

    public void FreeDriver(int orderId, int driverId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        String operationTime = LocalDateTime.now().format(formatter);
        MongoCollection<Document> collection = mongoDatabase.getCollection("delivery_tracking");
        collection.updateOne(Filters.eq("order_id", orderId),
                Updates.combine(
                        Updates.set("delivered_time", operationTime),
                        Updates.set("status", "delivered")));

        // Select from driver database
        MongoCollection<Document> collection1 = mongoDatabase.getCollection("drivers");

        //Select from test driver database
        // MongoCollection<Document> collection1 = mongoDatabase.getCollection("test_drivers");
        collection1.updateOne(Filters.eq("driver_id", driverId), Updates.set("availability", "free"));
    }

    public Document findBestDriver() {
            while (true) {
                Document bestDriver = BestDriver();
                System.out.println("Finding Suitable Driver for This Order.");
                if (bestDriver != null) {
                    System.out.println("Matched Driver Found.");
                    return bestDriver;
                } else {
                    try {
                        TimeUnit.SECONDS.sleep(180);
                    } catch ( InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return null;
                    }
                }
            }
    }

    public Document BestDriver() {
        List<Field<?>> fields = Collections.singletonList(
                new Field<>("match_score",
                        new Document("$multiply",
                                Arrays.asList("$location_parameter", "$rating"))));

        // Select from drivers database
        MongoCollection<Document> collection = mongoDatabase.getCollection("drivers");

        //Select from test drivers database
        // MongoCollection<Document> collection = mongoDatabase.getCollection("test_drivers");
        return collection.aggregate(Arrays.asList(
                Aggregates.match(Filters.eq("availability", "free")),
                Aggregates.addFields(fields),
                Aggregates.sort(Sorts.ascending("match_score")),
                Aggregates.limit(1)
        )).first();
    }
}
