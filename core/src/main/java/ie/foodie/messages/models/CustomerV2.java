package ie.foodie.messages.models;

import ie.foodie.messages.MessageSerializable;

public class CustomerV2 implements MessageSerializable {
    private int customerId;
    private String customerAddress;
    private String customerPhone;
    private String uuid;
    private String msgType;


    public CustomerV2(String msgType, int customerId, String customerAddress, String customerPhone, String uuid) {
        this.msgType = msgType;
        this.customerId = customerId;
        this.customerAddress = customerAddress;
        this.customerPhone = customerPhone;
        this.uuid = uuid;
    }

    public CustomerV2() {
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMsgType() {
        return msgType;
    }
}
