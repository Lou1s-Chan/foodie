package ie.foodie.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import ie.foodie.messages.CustomerOrderMessage;
import ie.foodie.messages.OrderConfirmMessage;
import ie.foodie.messages.PaymentConfirmMessage;
import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ie.foodie.database.OrderDao.calculateOrderTotalPrice;

public class OrderMongodbDao {
    private MongoDatabase database;

    public OrderMongodbDao(String connectionString) {
        // create a database connection
        createConnection(connectionString);
    }

    private void createConnection(String connectionString) {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .serverApi(serverApi)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("foodie");
        // Initialize collections if needed
        // database.createCollection("orders");
        // database.createCollection("restaurants");
    }

    // Example method to insert a CustomerOrderMessage into MongoDB
    public OrderConfirmMessage insertCustomerOrderMessage(CustomerOrderMessage customerOrderMessage) {
        MongoCollection<Document> ordersCollection = database.getCollection("orders");
        MongoCollection<Document> countersCollection = database.getCollection("counters");

        // Retrieve and increment the orderID counter
        int orderId = getNextSequence("orderID", countersCollection);

        // Convert Order[] to List<Document> for MongoDB
        List<Document> orderDetailsList = new ArrayList<>();
        for (Order order : customerOrderMessage.getOrders()) {
            Document restaurantDoc = new Document()
                    .append("restaurantId", order.getRestaurant().getRestaurantId())
                    .append("restaurantPhone", order.getRestaurant().getRestaurantPhone())
                    .append("restaurantAddress", order.getRestaurant().getRestaurantAddress());

            List<Document> detailsList = new ArrayList<>();
            for (Order.OrderDetail detail : order.getOrderDetails()) {
                detailsList.add(new Document()
                        .append("foodId", detail.getFoodId())
                        .append("price", detail.getPrice())
                        .append("quantity", detail.getQuantity()));
            }

            orderDetailsList.add(new Document()
                    .append("restaurant", restaurantDoc)
                    .append("details", detailsList));
        }

        Document orderDoc = new Document()
                .append("orderID", orderId)
                .append("customerID", customerOrderMessage.getCustomer().getCustomerId())
                .append("customerAddress", customerOrderMessage.getCustomer().getCustomerAddress())
                .append("customerPhone", customerOrderMessage.getCustomer().getCustomerPhone())
                .append("totalPrice", calculateOrderTotalPrice(customerOrderMessage))
                .append("orderDetails", orderDetailsList)
                .append("status", "Pending");


        ordersCollection.insertOne(orderDoc);

        return new OrderConfirmMessage(orderId, orderDoc.getDouble("totalPrice"));
    }

    private int getNextSequence(String name, MongoCollection<Document> countersCollection) {
        // Find the counter document and increment it atomically
        Document counter = countersCollection.findOneAndUpdate(
                Filters.eq("_id", name),
                Updates.inc("seq", 1),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
        );

        // If counter document doesn't exist, it will be created with a sequence number of 1
        return counter != null ? counter.getInteger("seq") : 1;
    }


    public CustomerOrderMessage selectByOrderId(int orderId) {
        MongoCollection<Document> ordersCollection = database.getCollection("orders");

        // Query the orders collection by orderId
        Document orderDoc = ordersCollection.find(Filters.eq("orderID", orderId)).first();
        if (orderDoc == null) {
            return null; // Order not found
        }

        // Extract customer information
        Customer customer = new Customer(orderDoc.getInteger("customerID"),
                orderDoc.getString("customerAddress"),
                orderDoc.getString("customerPhone"));

        // Extract and process order details
        List<Order> orders = new ArrayList<>();
        List<Document> orderDetailsDocs = (List<Document>) orderDoc.get("orderDetails");
        if (orderDetailsDocs != null) {
            for (Document detailDoc : orderDetailsDocs) {
                Document restaurantDoc = (Document) detailDoc.get("restaurant");

                Order.Restaurant restaurant = new Order.Restaurant(
                        restaurantDoc.getInteger("restaurantId"),
                        restaurantDoc.getString("restaurantPhone"),
                        restaurantDoc.getString("restaurantAddress")
                );

                List<Order.OrderDetail> orderDetails = new ArrayList<>();
                List<Document> detailsList = (List<Document>) detailDoc.get("details");
                for (Document d : detailsList) {
                    Order.OrderDetail orderDetail = new Order.OrderDetail(
                            d.getInteger("foodId"),
                            d.getDouble("price"),
                            d.getInteger("quantity")
                    );
                    orderDetails.add(orderDetail);
                }

                orders.add(new Order(restaurant, orderDetails.toArray(new Order.OrderDetail[0])));
            }
        }

        return new CustomerOrderMessage(customer, orders.toArray(new Order[0]));
    }

    public boolean updatePaymentStatus(PaymentConfirmMessage paymentConfirmMessage) {
        MongoCollection<Document> ordersCollection = database.getCollection("orders");

        // Create a filter to find the order by its ID
        Bson filter = Filters.eq("orderId", paymentConfirmMessage.getOrderId());

        // Create an update operation to set the new status
        Bson updateOperation = Updates.set("status", paymentConfirmMessage.getStatus());

        // Perform the update
        try {
            UpdateResult result = ordersCollection.updateOne(filter, updateOperation);
            return result.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("Updating payment status failed: " + e.getMessage());
            return false;
        }
    }

}
