import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import java.time.Duration;
import akka.testkit.javadsl.TestKit;
import ie.foodie.messages.OrderDeliveringMessage;
import ie.foodie.messages.OrderDeliveryMessage;
import ie.foodie.services.DeliveryService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DeliveryServiceTest {
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
    public void testDelivery() {
        TestKit probe = new TestKit(system);
        ActorRef deliveryService = system.actorOf(Props.create(DeliveryService.class));

        int testOrderId = 999;
        FakeOrder fakeOrder = new FakeOrder();
        FakeCustomer fakeCustomer = new FakeCustomer();

        OrderDeliveryMessage testDeliveryMessage = new OrderDeliveryMessage(testOrderId, fakeOrder, fakeCustomer);
        deliveryService.tell(testDeliveryMessage, probe.getRef());

        // OrderDeliveringMessage response = probe.expectMsgClass(OrderDeliveringMessage.class);
        OrderDeliveringMessage response = probe.expectMsgClass(Duration.ofSeconds(20), OrderDeliveringMessage.class);

        assertEquals("DELIVERED", response.getStatus());
        assertEquals("Order delivered", response.getMessage());
        assertEquals(testOrderId, response.getOrderId());
    }
}
