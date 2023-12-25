import ie.foodie.messages.models.Order;

import java.util.Arrays;
import java.util.List;

public class FakeOrder extends Order {

    public FakeOrder() {
        super(new Restaurant(123, "555-0123", "123 Test Street"), createFakeOrderDetails());
    }

    private static List<OrderDetail> createFakeOrderDetails() {
        // Create some fake order details
        return Arrays.asList(
                new OrderDetail(1, 9.99, 2),
                new OrderDetail(2, 14.99, 1)
        );
    }
}
