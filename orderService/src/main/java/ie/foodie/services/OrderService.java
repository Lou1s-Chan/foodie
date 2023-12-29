package ie.foodie.services;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import ie.foodie.actors.ActorProvider;
import ie.foodie.actors.FoodieActor;
import ie.foodie.database.OrderMongodbDao;
import ie.foodie.messages.*;
import ie.foodie.messages.models.Order;
import ie.foodie.printer.MessagePrinter;

public class OrderService extends FoodieActor {
    private final OrderMongodbDao orderDao;

    private final ActorSelection deliveryActor;
    private final ActorSelection restaurantActor;
    private final ActorSelection paymentActor;


    public OrderService() {
        deliveryActor =
                ActorProvider.getDeliveryActor(getContext().getSystem());
        restaurantActor =
                ActorProvider.getRestaurantActor(getContext().getSystem());
        paymentActor =
                ActorProvider.getPaymentActor(getContext().getSystem());
        orderDao = new OrderMongodbDao("mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority");
    }

    //for test
    public OrderService(ActorSelection deliveryActor, ActorSelection restaurantActor,ActorSelection paymentActor,
                        OrderMongodbDao orderDao) {
        this.deliveryActor = deliveryActor;
        this.restaurantActor = restaurantActor;
        this.paymentActor = paymentActor;
        this.orderDao = orderDao;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RestaurantsResponse.class, msg -> msg.getUserRef().tell(msg, getSender()))
                .match(MenuItemsResponse.class, msg -> msg.getUserRef().tell(msg, getSender()))
                .match(RestaurantQueryMessage.class, msg -> restaurantActor.tell(msg, getSelf()))
                .match(OrderPaymentMessage.class, msg -> paymentActor.tell(msg, getSelf()))
                .match(CustomerOrderMessage.class, msg -> {
                    System.out.println("******** Received order message: " + msg.getCustomer().getCustomerId());
                    // calculate price and store to db
                    OrderConfirmMessage orderConfirmMessage =
                            orderDao.insertCustomerOrderMessage(msg);
                    System.out.println("order id: " + orderConfirmMessage.getOrderId());
                    CustomerOrderMessage returnValue =
                            orderDao.selectByOrderId(orderConfirmMessage.getOrderId());
                    MessagePrinter.printCustomerOrderMessage(returnValue);

                    getSender().tell(orderConfirmMessage, getSelf());
                })
                // .match(PaymentConfirmMessage.class, msg -> {
                .match(PaymentStatusMessage.class, msg -> {
                    msg.getUserRef().tell(msg, getSender());
                    System.out.println("******** Received payment message: ");
                    MessagePrinter.printPaymentConfirmMessage(msg);
                    // change status in db
                    boolean updatePaymentStatus = orderDao.updatePaymentStatus(msg);
                    if (!updatePaymentStatus) {
                        System.out.println("Update payment status for orderID " + msg.getOrderId() +
                                " is not successful.");
                    }

                    CustomerOrderMessage customerOrderMessage =
                            orderDao.selectByOrderId(msg.getOrderId());
                    // send order to restaurant
                    // send order to delivery
                    for (Order order : customerOrderMessage.getOrders()) {
                        // print out restaurant message sent
                        System.out.println("******** Sending restaurant message: ");
                        RestaurantOrderMessage restaurantOrderMessage = new
                                RestaurantOrderMessage(customerOrderMessage.getCustomer().getCustomerId(),
                                order, msg.getUserRef());
                        MessagePrinter.printRestaurantOrderMessage(restaurantOrderMessage);
                        restaurantActor.tell(restaurantOrderMessage, getSelf());
                        // print out delivery message sent

                        System.out.println("******** Sending delivery message: ");
                        OrderDeliveryMessage orderDeliveryMessage = new
                                OrderDeliveryMessage(msg.getOrderId(), order,
                                customerOrderMessage.getCustomer(), msg.getUserRef());
                        deliveryActor.tell(orderDeliveryMessage, getSelf());
                        MessagePrinter.printOrderDeliveryMessage(orderDeliveryMessage);
                    }
                })
                .match(DeliveryQueryMessage.class, msg -> {
                    msg.getUserRef().tell(msg, getSelf());
                })
                .build();
    }
}
