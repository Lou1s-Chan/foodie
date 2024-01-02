package ie.foodie.messages;

import java.util.List;

import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;

public class CustomerOrderMessage implements MessageSerializable {
    private Customer customer;
    private List<Order> orders;
    private String msgType;

    public CustomerOrderMessage(Customer customer, List<Order> orders) {
        this.msgType = "CustomerOrderMessage";
        this.customer = customer;
        this.orders = orders;
    }

    public CustomerOrderMessage() {
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public String getMsgType() {
        return msgType;
    }
}
