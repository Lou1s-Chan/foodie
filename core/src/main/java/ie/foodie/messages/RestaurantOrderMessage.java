package ie.foodie.messages;

import akka.actor.ActorRef;
import ie.foodie.messages.models.Order;

public class RestaurantOrderMessage implements MessageSerializable {
    private int customerId;
    private Order order;
    private ActorRef userRef;

    public RestaurantOrderMessage(int customerId, Order order, ActorRef userRef) {
        this.customerId = customerId;
        this.order = order;
        this.userRef = userRef;
    }
    public RestaurantOrderMessage() {
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public ActorRef getUserRef() {
        return userRef;
    }

    public void setUserRef(ActorRef userRef) {
        this.userRef = userRef;
    }
}

