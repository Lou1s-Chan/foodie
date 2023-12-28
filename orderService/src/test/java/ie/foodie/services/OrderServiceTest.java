package ie.foodie.services;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestKit;
import akka.testkit.TestProbe;
import ie.foodie.database.OrderMongodbDao;
import ie.foodie.messages.*;
import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;
import org.junit.*;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class OrderServiceTest {
    static ActorSystem system;
    private final OrderMongodbDao orderDao = new OrderMongodbDao("mongodb+srv://foodie:DNiaOZmFoCMouwLG@foodie-test.odgtesd.mongodb.net/?retryWrites=true&w=majority");

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system, Duration.apply(2, TimeUnit.SECONDS), false);
        system = null;
    }

    @After
    public void cleanUpDb() {
        orderDao.dropDb();
    }

    @Test
    public void testSendCustomerOrderMessage() {
        CustomerOrderMessage customerOrderMessage = generateCustomerOrderMessage();
        final Props props = Props.create(OrderService.class);
        final ActorRef orderService = system.actorOf(props); // orderService
        final TestKit customer = new TestKit(system); // customer

        // server send a RegisterMessage to broker
        orderService.tell(customerOrderMessage, customer.testActor());
        OrderConfirmMessage orderConfirmMessage = customer.expectMsgClass(FiniteDuration.apply(10, TimeUnit.SECONDS), OrderConfirmMessage.class);

//        Assert.assertEquals(1, orderConfirmMessage.getOrderId());
        Assert.assertEquals(91.98, orderConfirmMessage.getTotalPrice(), 0);
    }

    @Test
    @Ignore
    public void testReceivePaymentConfirmMessage() {
        CustomerOrderMessage orderConfirmMessage = generateCustomerOrderMessage();
        PaymentStatusMessage paymentConfirmMessage = genereatePaymentConfirmMessage(1);

        orderDao.insertCustomerOrderMessage(orderConfirmMessage);
        final TestKit deliveryService = new TestKit(system); // delivery service
        final TestKit restaurantService = new TestKit(system); // restaurant service
        final TestKit userService = new TestKit(system); // user service
        final Props props = Props.create(OrderService.class, deliveryService.testActor(), restaurantService.testActor(), userService.testActor(), orderDao);
        final ActorRef orderService = system.actorOf(props); // orderService
        final TestKit paymentService = new TestKit(system); // payment service

        orderService.tell(paymentConfirmMessage, paymentService.testActor());

        deliveryService.expectMsgClass(FiniteDuration.apply(10, TimeUnit.SECONDS), OrderDeliveryMessage.class);
        restaurantService.expectMsgClass(FiniteDuration.apply(10, TimeUnit.SECONDS), RestaurantOrderMessage.class);
    }

    private PaymentStatusMessage genereatePaymentConfirmMessage(int orderId) {
        return new PaymentStatusMessage(orderId, "CARD-PAID", "Payment processed successfully.");
    }

    private CustomerOrderMessage generateCustomerOrderMessage() {
        // Create a customer
        Customer customer = new Customer(1, "123 Main Street", "555-1234");

        // Create a restaurant
        Order.Restaurant restaurant1 = new Order.Restaurant(101, "555-1001", "456 Park Ave");
        Order.Restaurant restaurant2 = new Order.Restaurant(102, "555-1002", "789 Broadway");

        // Create order details for restaurant 1
        Order.OrderDetail[] detailsForRestaurant1 = {
                new Order.OrderDetail(501, 9.99, 2), // 2 units of food item 501
                new Order.OrderDetail(502, 12.50, 1) // 1 unit of food item 502
        };

        // Create order details for restaurant 2
        Order.OrderDetail[] detailsForRestaurant2 = {
                new Order.OrderDetail(503, 15.00, 3), // 3 units of food item 503
                new Order.OrderDetail(504, 7.25, 2)  // 2 units of food item 504
        };

        // Create orders for each restaurant
        Order order1 = new Order(restaurant1, Arrays.asList(detailsForRestaurant1));
        Order order2 = new Order(restaurant2, Arrays.asList(detailsForRestaurant2));

        // Array of orders
        Order[] orders = {order1, order2};

        // Create the CustomerOrderMessage
        return new CustomerOrderMessage(customer, Arrays.asList(orders));
    }
}
