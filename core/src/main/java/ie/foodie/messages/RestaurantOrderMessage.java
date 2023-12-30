package ie.foodie.messages;

import ie.foodie.messages.models.Order;

public class RestaurantOrderMessage implements MessageSerializable {
    private int customerId;
    private Order order;
    private int orderid;

    public RestaurantOrderMessage(int customerId, Order order, int orderid) {
        this.customerId = customerId;
        this.order = order;
        this.orderid = orderid;
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

    public int getOrderID() {return this.orderid;}

    public void setOrder(Order order) {
        this.order = order;
    }
}

