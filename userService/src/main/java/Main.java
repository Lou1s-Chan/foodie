import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import service.Login;
import service.UserActor;
import service.core.ClientInfo;
import service.message.ClientMessage;
import service.message.OrderQueryMessage;
import service.message.RegisterMessage;

public class Main {
    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create();
        final ActorRef ref = system.actorOf(
                Props.create(UserActor.class),
                "user");
        ActorSelection selection = system
                .actorSelection("akka.tcp://default@order:2550/user/order");
        System.out.println("user make an order to order system");
        selection.tell((new OrderQueryMessage(001, 00001, 18.8, "123 Main St", "123456789")), ref);

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

        Login loginHandler = new Login();
        // Example login attempts
        loginHandler.login("exampleUser", "password123"); // Successful login
        loginHandler.login("invalidUser", "wrongPassword"); // Failed login
    }

}
