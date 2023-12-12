package service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import akka.actor.Props;
import ie.foodie.messages.*;
import ie.foodie.messages.models.Order;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import service.ResActor;

import java.time.Duration;

public class RestTests implements MessageSerializable {
    static ActorSystem system;
    @BeforeClass
    public static void setup() {system = ActorSystem.create(); }
    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }
    @Test
    public void quoterTest() {
        String pathToDB = "jdbc:sqlite:database/restaurantdatabase.db";
        final Props props = Props.create(ResActor.class, pathToDB);
        final ActorRef subject = system.actorOf(props);
        final TestKit probe = new TestKit(system);

        Order.OrderDetail[] orderDetails = new Order.OrderDetail[]{
                new Order.OrderDetail(1206, 8.99, 2),
                new Order.OrderDetail(1207, 10.50, 1),
                new Order.OrderDetail(1208, 7, 3)
        };

        RestaurantOrderMessage testOrder = new RestaurantOrderMessage(
                1, new Order(
                new Order.Restaurant(
                        1, "555-1234", "123 Oak Street"), orderDetails));

        RestaurantQueryMessage testQuery = new RestaurantQueryMessage(RestaurantQueryMessage.QueryType.RESTAURANT_LIST);
        RestaurantQueryMessage testMenuRequest = new RestaurantQueryMessage(RestaurantQueryMessage.QueryType.MENU_REQUEST, 1);
        subject.tell(testOrder, probe.getRef());
//        subject.tell(testQuery, probe.getRef());
//        subject.tell(testMenuRequest, probe.getRef());

        String response = probe.expectMsgClass(Duration.ofSeconds(2), String.class);
//        RestaurantsResponse response = probe.expectMsgClass(Duration.ofSeconds(2), RestaurantsResponse.class);
//        MenuItemsResponse response = probe.expectMsgClass(Duration.ofSeconds(2), MenuItemsResponse.class);
        System.out.println(response);
    }
}
