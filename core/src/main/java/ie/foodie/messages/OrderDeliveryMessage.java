package ie.foodie.messages;

import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;

public class OrderDeliveryMessage implements MessageSerializable {
    private Order order;
    private Customer customer;

    public OrderDeliveryMessage(Order order, Customer customer) {
        this.order = order;
        this.customer = customer;
    }

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
