package ie.foodie.services;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;
import com.sun.org.apache.xpath.internal.operations.Or;
import ie.foodie.CustomerOrderMessagePrinter;
import ie.foodie.actors.ActorProvider;
import ie.foodie.database.OrderDao;
import ie.foodie.messages.*;
import ie.foodie.messages.models.Order;

public class OrderService extends AbstractActor {
    private final OrderDao orderDao = new OrderDao();
    private final ActorSelection deliveryActor = ActorProvider.getDeliveryActor(getContext().getSystem());
    private final ActorSelection restaurantActor = ActorProvider.getRestaurantActor(getContext().getSystem());

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CustomerOrderMessage.class, msg -> {
                    System.out.println("received order message: " + msg.getCustomer().getCustomerId());
                    // calculate price and store to db
                    OrderConfirmMessage orderConfirmMessage = orderDao.insertCustomerOrderMessage(msg);
                    System.out.println("order id: " + orderConfirmMessage.getOrderId());
                    CustomerOrderMessage returnValue = orderDao.selectByOrderId(orderConfirmMessage.getOrderId());
                    CustomerOrderMessagePrinter.printCustomerOrderMessage(returnValue);

                    getSender().tell(orderConfirmMessage, getSelf());
                })
                .match(PaymentConfirmMessage.class, msg -> {
                    System.out.println("received payment message with orderID: " + msg.getOrderId() + ", status: " + msg.getStatus());
                    // change status in db
                    boolean updatePaymentStatus = orderDao.updatePaymentStatus(msg);
                    if (!updatePaymentStatus) {
                        System.out.println("Update payment status for orderID " + msg.getOrderId() + "is not successful.");
                    }

                    CustomerOrderMessage customerOrderMessage = orderDao.selectByOrderId(msg.getOrderId());
                    // send order to restaurant
                    // send order to delivery
                    for (Order order: customerOrderMessage.getOrders()) {
                        restaurantActor.tell(new RestaurantOrderMessage(customerOrderMessage.getCustomer().getCustomerId(), order), getSelf());
                        deliveryActor.tell(new OrderDeliveryMessage(order, customerOrderMessage.getCustomer()), getSelf());
                    }
                })
                .build();
    }


}
