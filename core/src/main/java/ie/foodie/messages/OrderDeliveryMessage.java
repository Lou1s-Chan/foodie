package ie.foodie.messages;

import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;

public class OrderDeliveryMessage implements MessageSerializable {
    int orderId;
    private Order order;
    private Customer customer;

    public OrderDeliveryMessage(int orderId, Order order, Customer customer) {
        this.orderId = orderId;
        this.order = order;
        this.customer = customer;
    }

    public int getOrderId() {return orderId;}
    public void setOrderId(int orderId) {this.orderId = orderId;}
    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
