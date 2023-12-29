package ie.foodie.messages;

import akka.actor.ActorRef;

public class OrderPaymentMessage implements MessageSerializable {
    private int orderId;
    private int customerID;
    private double totalPrice;
    private String paymentMethod;
    private ActorRef userRef;

    public OrderPaymentMessage(int orderId, int customerID, double totalPrice, String paymentMethod, ActorRef userRef) {
        this.orderId = orderId;
        this.customerID = customerID;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
        this.userRef = userRef;
    }

    public OrderPaymentMessage() {
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
    return customerID;
    }

    public void setCustomerId(int customerID) {
        this.customerID = customerID;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public ActorRef getUserRef() {
        return userRef;
    }

    public void setUserRef(ActorRef userRef) {
        this.userRef = userRef;
    }
}
