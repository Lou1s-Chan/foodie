package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;

// create/edit user MongoDB
public class ConnectMongoDB {
    public static void main(String[] args) {

        String uri = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";

        try (
                MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase database = mongoClient.getDatabase("foodie");
            insertUserTable(database);

            System.out.println("Sample data inserted successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void insertUserTable(MongoDatabase database) throws SQLException {
        MongoCollection<Document> collection = database.getCollection("users");
        long collectionSize = collection.countDocuments();
        System.out.println("Collection size: " + collectionSize);
        List<Document> userData = new ArrayList<>();

        // String[] userInfos = {
        // "INSERT INTO user ( username,password, phone, address) VALUES
        // ('Moo','password1', '123456789', 'Dublin 1');"

        // };
        String[] userInfos = {
                "Moo,password1, 123456789, Dublin 1"

        };

        for (String line : userInfos) {
            String[] parts = line.split(",");
            if (parts.length == 4) {
                String username = parts[0].trim();
                if (collection.countDocuments(eq("username", username)) == 0) {
                    Document user = new Document("username", username)
                            .append("password", parts[1].trim())
                            .append("telephone", parts[2].trim())
                            .append("address", parts[3].trim());
                    userData.add(user);
                }
            }
        }
        collection.insertMany(userData);
    }
}
