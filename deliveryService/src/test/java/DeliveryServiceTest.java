import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.Props;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

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
        system.eventStream().subscribe(probe.getRef(), DeadLetter.class);

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

        List<Object> deadLetters = probe.receiveN(9, Duration.ofSeconds(300));
        for (Object deadLetterObject : deadLetters) {
            DeadLetter deadLetter = (DeadLetter) deadLetterObject;

            // Check if the dead letter is of the expected type
            if (deadLetter.message() instanceof DeliveryCompleteMessage) {
                DeliveryCompleteMessage message = (DeliveryCompleteMessage) deadLetter.message();
                assertEquals("Delivered", message.getStatus());


                if (message.getOrderId() == 990) {
                    System.out.println("Dead letter log: Order 990 Delivered Successfully!");
                } else if (message.getOrderId() == 991) {
                    System.out.println("Dead letter log: Order 991 Delivered Successfully!");
                } else if (message.getOrderId() == 992) {
                    System.out.println("Dead letter log: Order 992 Delivered Successfully!");
                }
            } else if (deadLetter.message() instanceof DeliveryQueryMessage) {
                DeliveryQueryMessage message = (DeliveryQueryMessage) deadLetter.message();
                if (Objects.equals(message.getStatus(), "Pending")) {
                    if (message.getOrderId() == 990) {
                        System.out.println("Test log: Task of Order 990 Created.");
                    } else if (message.getOrderId() == 991) {
                        System.out.println("Test log: Task of Order 991 Created.");
                    } else if (message.getOrderId() == 992) {
                        System.out.println("Test log: Task of Order 992 Created.");
                    }
                } else if (Objects.equals(message.getStatus(), "Dispatched")) {
                    if (message.getOrderId() == 990) {
                        System.out.println("Test log: Order 990 Dispatched.");
                    } else if (message.getOrderId() == 991) {
                        System.out.println("Test log: Order 991 Dispatched.");
                    } else if (message.getOrderId() == 992) {
                        System.out.println("Test log: Order 992 Dispatched.");
                    }
                }
            }
        }
//
//
//        List<Object> responsesFirst = probe.receiveN(3, Duration.ofSeconds(150));
//        for (Object response1 : responsesFirst) {
//            assertTrue(response1 instanceof DeliveryQueryMessage);
//            DeliveryQueryMessage deliveryQueryMessage = (DeliveryQueryMessage) response1;
//            assertEquals("Pending", deliveryQueryMessage.getStatus());
//            assertEquals("Allocating suitable driver.", deliveryQueryMessage.getMessage());
//
//            if (deliveryQueryMessage.getOrderId() == 990) {
//                System.out.println("Test log: Task of Order 990 Created.");
//            } else if (deliveryQueryMessage.getOrderId() == 991) {
//                System.out.println("Test log: Task of Order 991 Created.");
//            } else if (deliveryQueryMessage.getOrderId() == 992) {
//                System.out.println("Test log: Task of Order 992 Created.");
//            }
//        }
//
//        List<Object> responsesSecond = probe.receiveN(3, Duration.ofSeconds(300));
//        for (Object response2 : responsesSecond) {
//            assertTrue(response2 instanceof DeliveryQueryMessage);
//            DeliveryQueryMessage deliveryQueryMessage = (DeliveryQueryMessage) response2;
//            assertEquals("Dispatched", deliveryQueryMessage.getStatus());
//            assertEquals("Order is on its way.", deliveryQueryMessage.getMessage());
//
//            if (deliveryQueryMessage.getOrderId() == 990) {
//                System.out.println("Test log: Order 990 Dispatched.");
//            } else if (deliveryQueryMessage.getOrderId() == 991) {
//                System.out.println("Test log: Order 991 Dispatched.");
//            } else if (deliveryQueryMessage.getOrderId() == 992) {
//                System.out.println("Test log: Order 992 Dispatched.");
//            }
//        }
//
//        List<Object> responsesThird = probe.receiveN(3, Duration.ofSeconds(300));
//        for (Object response : responsesThird) {
//            assertTrue(response instanceof DeliveryCompleteMessage);
//            DeliveryCompleteMessage deliveryCompleteMessage = (DeliveryCompleteMessage) response;
//            assertEquals("Delivered", deliveryCompleteMessage.getStatus());
//
//            if (deliveryCompleteMessage.getOrderId() == 990) {
//                System.out.println("Test log: Order 990 Delivered Successfully!");
//            } else if (deliveryCompleteMessage.getOrderId() == 991) {
//                System.out.println("Test log: Order 991 Delivered Successfully!");
//            }
//            else if (deliveryCompleteMessage.getOrderId() == 992) {
//                System.out.println("Test log: Order 992 Delivered Successfully!");
//            }
//        }

        system.eventStream().unsubscribe(probe.getRef(), DeadLetter.class);
    }
}
