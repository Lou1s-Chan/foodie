package ie.foodie.messages;

public class PaymentStatusMessage implements MessageSerializable {
    private int orderId;
    private String status;
    private String message;

    public PaymentStatusMessage(int orderId, String status, String message) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
    }

    public PaymentStatusMessage() {
    }

    public int getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "PaymentStatusMessage{" +
                "orderId=" + orderId +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
