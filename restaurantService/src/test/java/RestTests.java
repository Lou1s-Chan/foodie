import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import akka.actor.Props;
import ie.foodie.messages.MessageSerializable;
import ie.foodie.messages.RestaurantOrderMessage;
import ie.foodie.messages.models.Order;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import service.ResQuoter;

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
        final Props props = Props.create(ResQuoter.class);
        final ActorRef subject = system.actorOf(props);
        final TestKit probe = new TestKit(system);

        Order.OrderDetail[] orderDetails = new Order.OrderDetail[]{
                new Order.OrderDetail(1056, 8.99, 2),
                new Order.OrderDetail(1057, 10.50, 1),
                new Order.OrderDetail(1058, 7, 3)
        };

        RestaurantOrderMessage testOrder = new RestaurantOrderMessage(
                1, new Order(
                new Order.Restaurant(
                        1, "555-1234", "123 Oak Street"), orderDetails));

        subject.tell(testOrder, probe.getRef());

        String response = probe.expectMsgClass(Duration.ofSeconds(2), String.class);
        System.out.println(response);
    }
}
