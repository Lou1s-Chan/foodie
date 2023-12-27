package ie.foodie.services;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import ie.foodie.actors.ActorProvider;
import ie.foodie.database.OrderMongodbDao;
import ie.foodie.messages.*;
import ie.foodie.messages.models.Order;
import ie.foodie.printer.MessagePrinter;

public class OrderService extends AbstractActor {
    private final OrderMongodbDao orderDao;
    private final ActorRef deliveryActor;
    private final ActorRef restaurantActor;
//    private ActorSelection deliveryActor;
//    private ActorSelection restaurantActor;


//    @Override
//    public void preStart() {
//        ActorSystem system = getContext().getSystem();
//        this.deliveryActor =
//                ActorProvider.getDeliveryActor(system);
//        this.restaurantActor =
//                ActorProvider.getRestaurantActor(system);
//    }
    public OrderService() {
        deliveryActor =
                ActorProvider.getDeliveryActor(getContext().getSystem()).anchor();
        restaurantActor =
                ActorProvider.getRestaurantActor(getContext().getSystem()).anchor();
        orderDao = new OrderMongodbDao("mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority");
    }

    //for test
//    public OrderService(ActorRef deliveryActor, ActorRef restaurantActor,
//                        OrderMongodbDao orderDao) {
//        this.deliveryActor = deliveryActor;
//        this.restaurantActor = restaurantActor;
//        this.orderDao = orderDao;
//    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
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
                                order);
                        MessagePrinter.printRestaurantOrderMessage(restaurantOrderMessage);
                        restaurantActor.tell(restaurantOrderMessage, getSelf());
                        // print out delivery message sent

                        System.out.println("******** Sending delivery message: ");
                        OrderDeliveryMessage orderDeliveryMessage = new
                                OrderDeliveryMessage(msg.getOrderId(), order,
                                customerOrderMessage.getCustomer());
                        deliveryActor.tell(orderDeliveryMessage, getSelf());
                        MessagePrinter.printOrderDeliveryMessage(orderDeliveryMessage);
                    }
                })
                .build();
    }

}
