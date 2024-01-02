import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.Props;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import akka.testkit.javadsl.TestKit;
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

        OrderDeliveryMessage testDeliveryMessage1 = new OrderDeliveryMessage(testOrderId1, fakeOrder1, fakeCustomer1, ActorRef.noSender());
        deliveryService.tell(testDeliveryMessage1, probe.getRef());

        OrderDeliveryMessage testDeliveryMessage2 = new OrderDeliveryMessage(testOrderId2, fakeOrder2, fakeCustomer2, ActorRef.noSender());
        deliveryService.tell(testDeliveryMessage2, probe.getRef());

        OrderDeliveryMessage testDeliveryMessage3 = new OrderDeliveryMessage(testOrderId3, fakeOrder3, fakeCustomer3, ActorRef.noSender());
        deliveryService.tell(testDeliveryMessage3, probe.getRef());

        List<Object> deadLetters = probe.receiveN(10, Duration.ofSeconds(300));
        for (Object deadLetterObject : deadLetters) {
            DeadLetter deadLetter = (DeadLetter) deadLetterObject;
            if (deadLetter.message() instanceof DeliveryQueryMessage) {
                DeliveryQueryMessage message = (DeliveryQueryMessage) deadLetter.message();
                if (Objects.equals(message.getStatus(), "Pending")) {
                    System.out.println("Test log: Task of Order " + message.getOrderId() + " Created.");
                } else if (Objects.equals(message.getStatus(), "Dispatched")) {
                    System.out.println("Test log: Order " + message.getOrderId() + " Dispatched.");
                } else if (Objects.equals(message.getStatus(), "Delivered")) {
                    System.out.println("Test log: Task of Order " + message.getOrderId() + " Delivered.");
                } else if (Objects.equals(message.getStatus(), "NoDriver")) {
                    System.out.println("Test log: Task of Order " + message.getOrderId() + " No Driver Assigned Currently.");
                }
            }
        }

        system.eventStream().unsubscribe(probe.getRef(), DeadLetter.class);
    }
}
