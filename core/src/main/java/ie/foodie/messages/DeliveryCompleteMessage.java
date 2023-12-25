package ie.foodie.messages;

public class DeliveryCompleteMessage implements MessageSerializable{
    private int orderId;
    private String status;
    public DeliveryCompleteMessage(int orderId, String status) {
        this.orderId = orderId;
        this.status = status;
    }

    public DeliveryCompleteMessage() {
    }

    public int getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {this.status = status;}

    @Override
    public String toString() {
        return "DeliveryQueryMessage{" +
                "orderId=" + orderId +
                ", status='" + status + '\'' +
                '}';
    }

}
