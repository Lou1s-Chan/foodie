package ie.foodie.messages;

public class OrderPaymentMessage implements MessageSerializable {
    private int orderId;
    private int customerID;
    private double totalPrice;
    private String paymentMethod;

    public OrderPaymentMessage(int orderId, int customerID, double totalPrice, String paymentMethod) {
        this.orderId = orderId;
        this.customerID = customerID;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
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
}
