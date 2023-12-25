package ie.foodie.messages;

public class DeliveryQueryMessage implements MessageSerializable {
    private int orderId;
    private String status;
    private String message;

    public DeliveryQueryMessage(int orderId, String status, String message) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
    }

    public DeliveryQueryMessage() {
    }

    public int getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {this.status = status;}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {this.message = message;}

    @Override
    public String toString() {
        return "DeliveryQueryMessage{" +
                "orderId=" + orderId +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
