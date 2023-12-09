package ie.foodie;

import ie.foodie.messages.CustomerOrderMessage;
import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;

public class CustomerOrderMessagePrinter {

    public static void printCustomerOrderMessage(CustomerOrderMessage customerOrderMessage) {
        if (customerOrderMessage == null) {
            System.out.println("No order information available.");
            return;
        }

        Customer customer = customerOrderMessage.getCustomer();
        System.out.println("Customer Details:");
        System.out.println("Customer ID: " + customer.getCustomerId());
        System.out.println("Customer Address: " + customer.getCustomerAddress());
        System.out.println("Customer Phone: " + customer.getCustomerPhone());

        Order[] orders = customerOrderMessage.getOrders();
        if (orders == null || orders.length == 0) {
            System.out.println("No order details available.");
            return;
        }

        System.out.println("\nOrder Details:");
        for (Order order : orders) {
            Order.Restaurant restaurant = order.getRestaurant();
            System.out.println("Restaurant ID: " + restaurant.getRestaurantId());
            System.out.println("Restaurant Phone: " + restaurant.getRestaurantPhone());
            System.out.println("Restaurant Address: " + restaurant.getRestaurantAddress());

            Order.OrderDetail[] orderDetails = order.getOrderDetails();
            if (orderDetails == null || orderDetails.length == 0) {
                System.out.println("No order details available for this restaurant.");
                continue;
            }

            System.out.println("Food Ordered:");
            for (Order.OrderDetail detail : orderDetails) {
                System.out.println(" - Food ID: " + detail.getFoodId() + ", Price: " + detail.getPrice() + ", Quantity: " + detail.getQuantity());
            }
        }
    }
}
