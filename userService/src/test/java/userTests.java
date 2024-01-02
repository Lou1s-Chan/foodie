import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.javadsl.TestKit;
import ie.foodie.messages.MenuItemsResponse;
import ie.foodie.messages.RestaurantQueryMessage;
import service.UserActor;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

public class userTests {

    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;
    private static ActorSystem system;
    private static Props props;
    private static ActorRef subject;
    private static TestKit probe;

    @BeforeClass
    public static void setUp() {
        String dbURL = "mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority";
        mongoClient = MongoClients.create(dbURL);
        database = mongoClient.getDatabase("foodie");
        collection = database.getCollection("users");
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testCheckLoginValidCredentials() {
        assertTrue(checkLogin("Moo", "password1"));
    }

    @Test
    public void testCheckLoginInvalidCredentials() {
        assertFalse(checkLogin("InvalidUser", "InvalidPassword"));
    }

    public static Boolean checkLogin(String username, String password) {
        // Query to find documents with the specified username and password
        Bson query = Filters.and(
                Filters.eq("username", username),
                Filters.eq("password", password));

        // Execute the query
        Document userDocument = collection.find(query).first();

        // Check if a document with the specified username and password was found
        return userDocument != null;
    }
}
