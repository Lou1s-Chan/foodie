package ie.foodie.services;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import ie.foodie.printer.MessagePrinter;
import ie.foodie.actors.ActorProvider;
import ie.foodie.database.OrderDao;
import ie.foodie.messages.*;
import ie.foodie.messages.models.Order;

public class OrderService extends AbstractActor {
    private final OrderDao orderDao;
    private final ActorRef deliveryActor;
    private final ActorRef restaurantActor;

    public OrderService() {
        deliveryActor = ActorProvider.getDeliveryActor(getContext().getSystem()).anchor();
        restaurantActor = ActorProvider.getRestaurantActor(getContext().getSystem()).anchor();
        orderDao = new OrderDao();
    }

    //for test
    public OrderService(ActorRef deliveryActor, ActorRef restaurantActor, OrderDao orderDao) {
        this.deliveryActor = deliveryActor;
        this.restaurantActor = restaurantActor;
        this.orderDao = orderDao;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CustomerOrderMessage.class, msg -> {
                    System.out.println("******** Received order message: " + msg.getCustomer().getCustomerId());
                    // calculate price and store to db
                    OrderConfirmMessage orderConfirmMessage = orderDao.insertCustomerOrderMessage(msg);
                    System.out.println("order id: " + orderConfirmMessage.getOrderId());
                    CustomerOrderMessage returnValue = orderDao.selectByOrderId(orderConfirmMessage.getOrderId());
                    MessagePrinter.printCustomerOrderMessage(returnValue);

                    getSender().tell(orderConfirmMessage, getSelf());
                })
                .match(PaymentConfirmMessage.class, msg -> {
                    System.out.println("******** Received payment message: ");
                    MessagePrinter.printPaymentConfirmMessage(msg);
                    // change status in db
                    boolean updatePaymentStatus = orderDao.updatePaymentStatus(msg);
                    if (!updatePaymentStatus) {
                        System.out.println("Update payment status for orderID " + msg.getOrderId() + "is not successful.");
                    }

                    CustomerOrderMessage customerOrderMessage = orderDao.selectByOrderId(msg.getOrderId());
                    // send order to restaurant
                    // send order to delivery
                    for (Order order: customerOrderMessage.getOrders()) {
                        // print out restaurant message sent
                        System.out.println("******** Sending restaurant message: ");
                        RestaurantOrderMessage restaurantOrderMessage = new RestaurantOrderMessage(customerOrderMessage.getCustomer().getCustomerId(), order);
                        MessagePrinter.printRestaurantOrderMessage(restaurantOrderMessage);
                        restaurantActor.tell(restaurantOrderMessage, getSelf());
                        // print out delivery message sent

                        System.out.println("******** Sending delivery message: ");
                        OrderDeliveryMessage orderDeliveryMessage = new OrderDeliveryMessage(msg.getOrderId(), order, customerOrderMessage.getCustomer());
                        deliveryActor.tell(orderDeliveryMessage, getSelf());
                        MessagePrinter.printOrderDeliveryMessage(orderDeliveryMessage);
                    }
                })
                .build();
    }


}
