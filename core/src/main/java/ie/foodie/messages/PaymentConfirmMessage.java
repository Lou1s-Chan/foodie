package ie.foodie.messages;

public class PaymentConfirmMessage implements MessageSerializable {
    int orderId;
    String status;

    public PaymentConfirmMessage(int orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public PaymentConfirmMessage() {
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "PaymentConfirmMessage{" +
                "orderId=" + orderId +
                ", status='" + status + '\'' +
                '}';
    }
}
