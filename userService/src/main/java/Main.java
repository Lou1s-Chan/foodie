import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ie.foodie.messages.CustomerOrderMessage;
import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;
import ie.foodie.messages.models.Order.OrderDetail;
import ie.foodie.messages.models.Order.Restaurant;
import service.Login;
import service.UserActor;
import service.message.OrderQueryMessage;

public class Main {
    public static void main(String[] args) {

        // creating the system and actor for USER
        ActorSystem system = ActorSystem.create("user-system");
        final ActorRef ref = system.actorOf(
                Props.create(UserActor.class),
                "user-ref");

        // send message to ORDER in different system
        ActorSelection selection = system
                .actorSelection("akka.tcp://order-system@order-host:2553/user/order-service");
        System.out.println("user make an order to order system");

        Restaurant restaurant1 = new Restaurant(1, "123456789", "Dublin1");
        OrderDetail[] orderDetail1 = { new OrderDetail(1, 18.88, 3), new OrderDetail(2, 28.88, 5) };
        Restaurant restaurant2 = new Restaurant(2, "987654321", "Dublin2");
        OrderDetail[] orderDetail2 = { new OrderDetail(3, 7.99, 2), new OrderDetail(4, 59.99, 4) };
        Order[] order1 = { new Order(restaurant1, orderDetail1), new Order(restaurant2, orderDetail2) };
        selection.tell(new CustomerOrderMessage(new Customer(1, "Dublin1", "123456789"), order1), ref);

        // // Creating a User object
        // User user1 = new User(1, "john_doe", "john@example.com");

        // // Accessing attributes using getter methods
        // System.out.println("User ID: " + user1.getUserId());
        // System.out.println("Username: " + user1.getUsername());
        // System.out.println("Email: " + user1.getEmail());

        // // Updating attributes using setter methods
        // user1.setUsername("john_doe_updated");
        // System.out.println("Updated Username: " + user1.getUsername());

        // // Printing the entire User object using toString method
        // System.out.println("User Details: " + user1);

        // Login loginHandler = new Login();
        // // Example login attempts
        // loginHandler.login("exampleUser", "password123"); // Successful login
        // loginHandler.login("invalidUser", "wrongPassword"); // Failed login
    }

}
