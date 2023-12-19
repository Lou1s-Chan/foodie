package service.utility;

import static com.mongodb.client.model.Filters.eq;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MongoDBDataHandler {
    public static void main( String[] args ) {

        String uri =  "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("foodie");

            // Insert sample data into restaurants table
//            insertDataIntoRestaurants(database);

            // Insert sample data into menu_items table
            insertDataIntoMenuItems(database);

            System.out.println("Sample data inserted successfully.");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertDataIntoRestaurants(MongoDatabase database) {
        MongoCollection<Document> collection = database.getCollection("restaurants");
        List<Document> restaurantData = new ArrayList<>();

        String[] sampleData = {
                "1,Gourmet Delight,123 Oak Street,Fine dining with a cozy atmosphere.,555-1234,www.gourmetdelight.com",
                "2,Pizza Palace,456 Pine Avenue,Delicious Italian pizza and pasta.,555-5678,www.pizzapalace.com",
                "3,The Curry House,789 Maple Lane,Authentic Indian curries and tandoori.,555-9012,www.thecurryhouse.com",
                "4,Gourmet Delight #4,382 New Street,Fine dining with a cozy atmosphere.,555-1234,www.gourmetdelight.com",
                "5,Pizza Palace #5,287 New Street,Delicious Italian pizza and pasta.,555-5678,www.pizzapalace.com",
                "6,The Curry House #6,713 New Street,Authentic Indian curries and tandoori.,555-9012,www.thecurryhouse.com",
                "7,Gourmet Delight #7,418 New Street,Fine dining with a cozy atmosphere.,555-1234,www.gourmetdelight.com",
                "8,Pizza Palace #8,285 New Street,Delicious Italian pizza and pasta.,555-5678,www.pizzapalace.com",
                "9,The Curry House #9,202 New Street,Authentic Indian curries and tandoori.,555-9012,www.thecurryhouse.com",
                "10,Gourmet Delight #10,51 New Street,Fine dining with a cozy atmosphere.,555-1234,www.gourmetdelight.com",
                "11,Pizza Palace #11,583 New Street,Delicious Italian pizza and pasta.,555-5678,www.pizzapalace.com",
                "12,The Curry House #12,349 New Street,Authentic Indian curries and tandoori.,555-9012,www.thecurryhouse.com",
                "13,Gourmet Delight #13,133 New Street,Fine dining with a cozy atmosphere.,555-1234,www.gourmetdelight.com",
                "14,Pizza Palace #14,295 New Street,Delicious Italian pizza and pasta.,555-5678,www.pizzapalace.com",
                "15,The Curry House #15,176 New Street,Authentic Indian curries and tandoori.,555-9012,www.thecurryhouse.com",
                "16,Gourmet Delight #16,829 New Street,Fine dining with a cozy atmosphere.,555-1234,www.gourmetdelight.com",
                "17,Pizza Palace #17,456 New Street,Delicious Italian pizza and pasta.,555-5678,www.pizzapalace.com",
                "18,The Curry House #18,182 New Street,Authentic Indian curries and tandoori.,555-9012,www.thecurryhouse.com",
                "19,Gourmet Delight #19,872 New Street,Fine dining with a cozy atmosphere.,555-1234,www.gourmetdelight.com",
                "20,Pizza Palace #20,941 New Street,Delicious Italian pizza and pasta.,555-5678,www.pizzapalace.com",
                "21,The Curry House #21,131 New Street,Authentic Indian curries and tandoori.,555-9012,www.thecurryhouse.com",
                "22,Gourmet Delight #22,246 New Street,Fine dining with a cozy atmosphere.,555-1234,www.gourmetdelight.com",
                "23,Pizza Palace #23,719 New Street,Delicious Italian pizza and pasta.,555-5678,www.pizzapalace.com",
                "24,The Curry House #24,962 New Street,Authentic Indian curries and tandoori.,555-9012,www.thecurryhouse.com",
                "25,Gourmet Delight #25,374 New Street,Fine dining with a cozy atmosphere.,555-1234,www.gourmetdelight.com",
                "26,Pizza Palace #26,158 New Street,Delicious Italian pizza and pasta.,555-5678,www.pizzapalace.com",
                "27,The Curry House #27,644 New Street,Authentic Indian curries and tandoori.,555-9012,www.thecurryhouse.com",
                "28,Gourmet Delight #28,913 New Street,Fine dining with a cozy atmosphere.,555-1234,www.gourmetdelight.com",
                "29,Pizza Palace #29,501 New Street,Delicious Italian pizza and pasta.,555-5678,www.pizzapalace.com",
                "30,The Curry House #30,318 New Street,Authentic Indian curries and tandoori.,555-9012,www.thecurryhouse.com"
        };

        for (String line : sampleData) {
            String[] parts = line.split(",");
            if (parts.length == 6) {
                int restaurantId = Integer.parseInt(parts[0].trim());
                if (collection.countDocuments(eq("id", restaurantId)) == 0) {
                    Document restaurant = new Document("id", restaurantId)
                            .append("name", parts[1].trim())
                            .append("address", parts[2].trim())
                            .append("description", parts[3].trim())
                            .append("phone", parts[4].trim())
                            .append("website", parts[5].trim());
                    restaurantData.add(restaurant);
                }
            }
        }

        collection.insertMany(restaurantData);
    }

    private static void insertDataIntoMenuItems(MongoDatabase database) {
        MongoCollection<Document> collection = database.getCollection("menu_items");
        collection.deleteMany(new Document()); // Clear existing data

        // Define sample menu item data
        List<String> menuItems = new ArrayList<>();
        menuItems.add("Margherita Pizza,Classic Margherita with fresh mozzarella and basil,8.99");
        menuItems.add("Spaghetti Carbonara,Creamy pasta with bacon and Parmesan cheese,10.50");
        menuItems.add("Caesar Salad,Crisp romaine lettuce with Caesar dressing and croutons,7.00");
        menuItems.add("Grilled Salmon,Grilled salmon fillet with a lemon butter sauce,15.00");
        menuItems.add("Tiramisu,Traditional Italian dessert with mascarpone and espresso,6.50");

        Random random = new Random();

        // Insert the sample menu items for each restaurant
        for (int restaurantId = 1; restaurantId <= 30; restaurantId++) {
            for (String menuItemData : menuItems) {
                String[] menuItemParts = menuItemData.split(",");
                if (menuItemParts.length == 3) {
                    String name = menuItemParts[0].trim();
                    String description = menuItemParts[1].trim();
                    double basePrice = Double.parseDouble(menuItemParts[2].trim());

                    // Randomize the price within +-2 range
                    double minPrice = basePrice - 2.0;
                    double maxPrice = basePrice + 2.0;
                    double price = minPrice + (maxPrice - minPrice) * random.nextDouble();

                    Document menuItem = new Document()
                            .append("restaurant_id", restaurantId)
                            .append("name", name)
                            .append("description", description)
                            .append("price", price);
                    collection.insertOne(menuItem);
                }
            }
        }
    }
}
