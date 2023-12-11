package ie.foodie.messages;

import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;

public class CustomerOrderMessage implements MessageSerializable {
    private Customer customer;
    private Order[] orders;

    public CustomerOrderMessage(Customer customer, Order[] orders) {
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

    public Order[] getOrders() {
        return orders;
    }

    public void setOrders(Order[] orders) {
        this.orders = orders;
    }
}
