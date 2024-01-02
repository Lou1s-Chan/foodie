package ie.foodie.messages;

public class ClientMessage implements MessageSerializable{
    private String msgType;
    private String message;
    private String uuid;
    public ClientMessage(String msgType, String uuid, String message) {
        this.msgType = msgType;
        this.uuid = uuid;
        this.message = message;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }
}
