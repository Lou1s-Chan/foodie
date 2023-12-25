import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import java.time.Duration;
import java.util.List;
import akka.testkit.javadsl.TestKit;
import ie.foodie.messages.DeliveryCompleteMessage;
import ie.foodie.messages.DeliveryQueryMessage;
import ie.foodie.messages.OrderDeliveryMessage;
import ie.foodie.services.DeliveryService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        int testOrderId1 = 990;
        FakeOrder fakeOrder1 = new FakeOrder();
        FakeCustomer fakeCustomer1 = new FakeCustomer();

        int testOrderId2 = 991;
        FakeOrder fakeOrder2 = new FakeOrder();
        FakeCustomer fakeCustomer2 = new FakeCustomer();

        OrderDeliveryMessage testDeliveryMessage1 = new OrderDeliveryMessage(testOrderId1, fakeOrder1, fakeCustomer1);
        deliveryService.tell(testDeliveryMessage1, probe.getRef());

        OrderDeliveryMessage testDeliveryMessage2 = new OrderDeliveryMessage(testOrderId2, fakeOrder2, fakeCustomer2);
        deliveryService.tell(testDeliveryMessage2, probe.getRef());

        List<Object> responsesFirst = probe.receiveN(2, Duration.ofSeconds(10));
        for (Object response : responsesFirst) {
            assertTrue(response instanceof DeliveryQueryMessage);
            DeliveryQueryMessage deliveryQueryMessage = (DeliveryQueryMessage) response;
            assertEquals("DISPATCHED", deliveryQueryMessage.getStatus());
            assertEquals("Order is on its way.", deliveryQueryMessage.getMessage());

            if (deliveryQueryMessage.getOrderId() == 990) {
                System.out.println("Test log: Order 990 Dispatched Successfully!");
            } else if (deliveryQueryMessage.getOrderId() == 991) {
                System.out.println("Test log: Order 991 Dispatched Successfully!");
            }
        }

        List<Object> responsesSecond = probe.receiveN(2, Duration.ofSeconds(65));
        for (Object response : responsesSecond) {
            assertTrue(response instanceof DeliveryCompleteMessage);
            DeliveryCompleteMessage deliveryCompleteMessage = (DeliveryCompleteMessage) response;
            assertEquals("DELIVERED", deliveryCompleteMessage.getStatus());

            if (deliveryCompleteMessage.getOrderId() == 990) {
                System.out.println("Test log: Order 990 Delivered Successfully!");
            } else if (deliveryCompleteMessage.getOrderId() == 991) {
                System.out.println("Test log: Order 991 Delivered Successfully!");
            }
        }

//        probe.within(Duration.ZERO, Duration.ofSeconds(10), () -> {
//            DeliveryQueryMessage response1 = probe.expectMsgClass(DeliveryQueryMessage.class);
//            assertEquals(testOrderId, response1.getOrderId());
//            assertEquals("DISPATCHED", response1.getStatus());
//            assertEquals("Order is on its way.", response1.getMessage());
//            return null;
//        });
//
//        probe.within(Duration.ZERO, Duration.ofSeconds(30), () -> {
//            DeliveryCompleteMessage response2 = probe.expectMsgClass(DeliveryCompleteMessage.class);
//            assertEquals(testOrderId, response2.getOrderId());
//            assertEquals("DELIVERED", response2.getStatus());
//            return null;
//        });


    }
}
