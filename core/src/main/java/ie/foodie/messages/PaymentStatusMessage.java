package ie.foodie.messages;

import akka.actor.ActorRef;

public class PaymentStatusMessage implements MessageSerializable {
    private int orderId;
    private String status;
    private String message;
    private ActorRef userRef;

    public PaymentStatusMessage(int orderId, String status, String message, ActorRef userRef) {
        this.orderId = orderId;
        this.status = status;
        this.message = message;
        this.userRef = userRef;
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

    public ActorRef getUserRef() {
        return userRef;
    }

    public void setUserRef(ActorRef userRef) {
        this.userRef = userRef;
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
