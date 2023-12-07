package ie.foodie.messages;

import ie.foodie.messages.models.Order;

public class RestaurantOrderMessage implements MessageSerializable {
    private int customerId;
    private Order order;

    public RestaurantOrderMessage(int customerId, Order order) {
        this.customerId = customerId;
        this.order = order;
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
}

