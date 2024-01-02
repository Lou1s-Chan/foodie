package ie.foodie.messages;

import akka.actor.ActorRef;

public class DeliveryQueryMessage implements MessageSerializable {
    private int orderId;
    private String status;
    private String message;
    private ActorRef userRef;
    private String msgType;

    public DeliveryQueryMessage(int orderId, String status, String message, ActorRef userRef) {
        this.msgType = "DeliveryQueryMessage";
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.userRef = userRef;
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

    public ActorRef getUserRef() {
        return userRef;
    }

    public void setUserRef(ActorRef userRef) {
        this.userRef = userRef;
    }

    public String getMsgType() {
        return msgType;
    }

    @Override
    public String toString() {
        return "DeliveryQueryMessage{" +
                "orderId=" + orderId +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
