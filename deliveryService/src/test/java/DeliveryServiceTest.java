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
import ie.foodie.services.DriverService;
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
        ActorRef driverService = system.actorOf(Props.create(DriverService.class), "driver-service");
        ActorRef deliveryService = system.actorOf(Props.create(DeliveryService.class), "delivery-service");

        int testOrderId1 = 990;
        FakeOrder fakeOrder1 = new FakeOrder();
        FakeCustomer fakeCustomer1 = new FakeCustomer();

        int testOrderId2 = 991;
        FakeOrder fakeOrder2 = new FakeOrder();
        FakeCustomer fakeCustomer2 = new FakeCustomer();

        int testOrderId3 = 992;
        FakeOrder fakeOrder3 = new FakeOrder();
        FakeCustomer fakeCustomer3 = new FakeCustomer();

        OrderDeliveryMessage testDeliveryMessage1 = new OrderDeliveryMessage(testOrderId1, fakeOrder1, fakeCustomer1);
        deliveryService.tell(testDeliveryMessage1, probe.getRef());

        OrderDeliveryMessage testDeliveryMessage2 = new OrderDeliveryMessage(testOrderId2, fakeOrder2, fakeCustomer2);
        deliveryService.tell(testDeliveryMessage2, probe.getRef());

        OrderDeliveryMessage testDeliveryMessage3 = new OrderDeliveryMessage(testOrderId3, fakeOrder3, fakeCustomer3);
        deliveryService.tell(testDeliveryMessage3, probe.getRef());

        List<Object> responsesFirst = probe.receiveN(3, Duration.ofSeconds(300));
        for (Object response : responsesFirst) {
            assertTrue(response instanceof DeliveryQueryMessage);
            DeliveryQueryMessage deliveryQueryMessage = (DeliveryQueryMessage) response;
            assertEquals("Pending", deliveryQueryMessage.getStatus());
            assertEquals("Allocating suitable driver.", deliveryQueryMessage.getMessage());

            if (deliveryQueryMessage.getOrderId() == 990) {
                System.out.println("Test log: Task of Order 990 Created.");
            } else if (deliveryQueryMessage.getOrderId() == 991) {
                System.out.println("Test log: Task of Order 991 Created.");
            } else if (deliveryQueryMessage.getOrderId() == 992) {
                System.out.println("Test log: Task of Order 992 Created.");
            }
        }

        List<Object> responsesSecond = probe.receiveN(3, Duration.ofSeconds(300));
        for (Object response : responsesSecond) {
            assertTrue(response instanceof DeliveryQueryMessage);
            DeliveryQueryMessage deliveryQueryMessage = (DeliveryQueryMessage) response;
            assertEquals("Dispatched", deliveryQueryMessage.getStatus());
            assertEquals("Order is on its way.", deliveryQueryMessage.getMessage());

            if (deliveryQueryMessage.getOrderId() == 990) {
                System.out.println("Test log: Order 990 Dispatched Successfully!");
            } else if (deliveryQueryMessage.getOrderId() == 991) {
                System.out.println("Test log: Order 991 Dispatched Successfully!");
            } else if (deliveryQueryMessage.getOrderId() == 992) {
                System.out.println("Test log: Order 992 Dispatched Successfully!");
            }
        }

        List<Object> responsesThird = probe.receiveN(3, Duration.ofSeconds(300));
        for (Object response : responsesThird) {
            assertTrue(response instanceof DeliveryCompleteMessage);
            DeliveryCompleteMessage deliveryCompleteMessage = (DeliveryCompleteMessage) response;
            assertEquals("Delivered", deliveryCompleteMessage.getStatus());

            if (deliveryCompleteMessage.getOrderId() == 990) {
                System.out.println("Test log: Order 990 Delivered Successfully!");
            } else if (deliveryCompleteMessage.getOrderId() == 991) {
                System.out.println("Test log: Order 991 Delivered Successfully!");
            }
            else if (deliveryCompleteMessage.getOrderId() == 992) {
                System.out.println("Test log: Order 992 Delivered Successfully!");
            }
        }
    }
}
