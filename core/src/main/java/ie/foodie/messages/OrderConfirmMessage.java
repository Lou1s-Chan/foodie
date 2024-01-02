package ie.foodie.messages;

public class OrderConfirmMessage implements MessageSerializable {
    int orderId;
    double totalPrice;
    String msgType;

    public OrderConfirmMessage(int orderId, double totalPrice) {
        this.msgType = "OrderConfirmMessage";
        this.orderId = orderId;
        this.totalPrice = totalPrice;
    }

    public OrderConfirmMessage() {
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
}
