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

import java.time.Duration;
import java.util.Arrays;

public class RestTests implements MessageSerializable {
        static ActorSystem system;

        @BeforeClass
        public static void setup() {
                system = ActorSystem.create();
        }

        @AfterClass
        public static void teardown() {
                TestKit.shutdownActorSystem(system);
                system = null;
        }

        @Test
        public void quoterTest() {
                final Props props = Props.create(ResActor.class);
                final ActorRef subject = system.actorOf(props);
                final TestKit probe = new TestKit(system);

                Order.OrderDetail[] orderDetails = new Order.OrderDetail[] {
                                new Order.OrderDetail(0, 8.99, 2),
                                new Order.OrderDetail(1, 10.50, 1),
                                new Order.OrderDetail(2, 7, 3)
                };

                RestaurantOrderMessage testOrder = new RestaurantOrderMessage(
                                1, new Order(
                                                new Order.Restaurant(
                                                                1, "555-1234", "123 Oak Street"),
                                                Arrays.asList(orderDetails)), 123,
                                ActorRef.noSender());

                RestaurantQueryMessage testQuery = new RestaurantQueryMessage(
                                RestaurantQueryMessage.QueryType.RESTAURANT_LIST, ActorRef.noSender());
                RestaurantQueryMessage testMenuRequest = new RestaurantQueryMessage(
                                RestaurantQueryMessage.QueryType.MENU_REQUEST, 3, ActorRef.noSender());
                // subject.tell(testOrder, probe.getRef());
                // subject.tell(testQuery, probe.getRef());
                subject.tell(testMenuRequest, probe.getRef());

                // String response = probe.expectMsgClass(Duration.ofSeconds(2), String.class);
                // RestaurantsResponse response = probe.expectMsgClass(Duration.ofSeconds(2),
                // RestaurantsResponse.class);
                MenuItemsResponse response = probe.expectMsgClass(Duration.ofSeconds(2), MenuItemsResponse.class);
                System.out.println(response);
        }
}
