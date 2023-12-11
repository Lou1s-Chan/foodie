package ie.foodie.messages;

public class OrderDeliveringMessage implements MessageSerializable {
    private final int orderId;
    private final String status;
    private final String message;

    public OrderDeliveringMessage(int orderId, String status, String message) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
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
        return "OrderDeliveringMessage{" +
                "orderId=" + orderId +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
