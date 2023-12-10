package ie.foodie.messages;

public class OrderPaymentMessage implements MessageSerializable {
    private int orderId;
    private double totalPrice;
    private String paymentMethod;

    public OrderPaymentMessage(int orderId, double totalPrice, String paymentMethod) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.paymentMethod = paymentMethod;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
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
