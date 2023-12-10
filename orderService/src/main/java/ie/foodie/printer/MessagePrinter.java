package ie.foodie.printer;

import ie.foodie.messages.CustomerOrderMessage;
import ie.foodie.messages.PaymentConfirmMessage;
import ie.foodie.messages.RestaurantOrderMessage;
import ie.foodie.messages.models.Customer;
import ie.foodie.messages.models.Order;

public class MessagePrinter {

    public static void printRestaurantOrderMessage(RestaurantOrderMessage message) {
        if (message == null) {
            System.out.println("RestaurantOrderMessage is null.");
            return;
        }

        System.out.println("Restaurant Order Message Details:");
        System.out.println("Customer ID: " + message.getCustomerId());

        Order order = message.getOrder();
        if (order != null) {
            printOrder(order);
        } else {
            System.out.println("Order details are not available.");
        }
    }

    private static void printOrder(Order order) {
        Order.Restaurant restaurant = order.getRestaurant();
        if (restaurant != null) {
            System.out.println("Restaurant ID: " + restaurant.getRestaurantId());
            System.out.println("Restaurant Phone: " + restaurant.getRestaurantPhone());
            System.out.println("Restaurant Address: " + restaurant.getRestaurantAddress());
        } else {
            System.out.println("Restaurant details are not available.");
        }

        Order.OrderDetail[] orderDetails = order.getOrderDetails();
        if (orderDetails != null) {
            System.out.println("Order Details:");
            for (Order.OrderDetail detail : orderDetails) {
                System.out.println(" - Food ID: " + detail.getFoodId() +
                        ", Price: " + detail.getPrice() +
                        ", Quantity: " + detail.getQuantity());
            }
        } else {
            System.out.println("Order items are not available.");
        }
    }
    public static void printPaymentConfirmMessage(PaymentConfirmMessage message) {
        if (message == null) {
            System.out.println("Message is null.");
        } else {
            System.out.println("PaymentConfirmMessage Details:");
            System.out.println("Order ID: " + message.getOrderId());
            System.out.println("Status: " + message.getStatus());
        }
    }
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
