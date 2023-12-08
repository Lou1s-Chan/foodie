package service.message;

public class OrderQueryMessage implements MySerializable {
    private int customerId;
    private int foodId;
    private double price;
    private String customerAddress;
    private String customerPhone;

    public OrderQueryMessage(int customerId, int foodId, double price, String customerAddress, String customerPhone) {
        this.customerId = customerId;
        this.foodId = foodId;
        this.price = price;
        this.customerAddress = customerAddress;
        this.customerPhone = customerPhone;
    }
}
