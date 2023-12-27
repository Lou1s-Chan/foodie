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
        List<Document> userData = new ArrayList<>();

        String[] userInfos = {
                "001,Moo,password1, 111111111, Dublin 1, ,",
                "002,John,password2, 222222222, Dublin 2, ,",
                "003,Jack,password3, 333333333, Dublin 3, ,",
                "004,James,password4, 444444444, Dublin 4, ,",
                "005,Joe,password5, 555555555, Dublin 5, ,"

        };

        for (String line : userInfos) {
            String[] parts = line.split(",");
            if (parts.length == 6) {
                String id = parts[0].trim();
                if (collection.countDocuments(eq("ID", id)) == 0) {
                    Document user = new Document("ID", id)
                            .append("username", parts[1].trim())
                            .append("password", parts[2].trim())
                            .append("telephone", parts[3].trim())
                            .append("address", parts[4].trim())
                            .append("cardNumber", parts[5].trim());
                    userData.add(user);
                }
            }
        }
        collection.insertMany(userData);
    }
}
