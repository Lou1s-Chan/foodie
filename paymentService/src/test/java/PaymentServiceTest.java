import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import ie.foodie.messages.OrderPaymentMessage;
import ie.foodie.messages.PaymentStatusMessage;
import service.PaymentService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import akka.actor.Props;

import static org.junit.Assert.assertEquals;

public class PaymentServiceTest {

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
    public void testPaymentProcessing() {
        TestKit probe = new TestKit(system);
        ActorRef paymentService = system.actorOf(Props.create(PaymentService.class));

        int testOrderId = 123;
        double testTotalPrice = 100.0;
        String testPaymentMethod = "Credit Card";

        OrderPaymentMessage testOrderPaymentMessage = new OrderPaymentMessage(testOrderId, testTotalPrice, testPaymentMethod);

        paymentService.tell(testOrderPaymentMessage, probe.getRef());

        PaymentStatusMessage response = probe.expectMsgClass(PaymentStatusMessage.class);

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("Payment processed successfully.", response.getMessage());
        assertEquals(testOrderId, response.getOrderId());
    }
}
