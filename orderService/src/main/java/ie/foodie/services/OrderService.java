package ie.foodie.services;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import com.fasterxml.jackson.databind.ObjectMapper;
import ie.foodie.actors.ActorProvider;
import ie.foodie.actors.FoodieActor;
import ie.foodie.database.OrderMongodbDao;
import ie.foodie.messages.*;
import ie.foodie.messages.models.Order;
import ie.foodie.printer.MessagePrinter;
import ie.foodie.SSE.SSEController;

public class OrderService extends FoodieActor {
    private final OrderMongodbDao orderDao;

    private final ActorSelection deliveryActor;
    private final ActorSelection restaurantActor;

    private ActorRef userActor = null;

    private SSEController sseController;

    public OrderService(SSEController sseController) {
        this.sseController = sseController;
        deliveryActor =
                ActorProvider.getDeliveryActor(getContext().getSystem());
        restaurantActor =
                ActorProvider.getRestaurantActor(getContext().getSystem());
        orderDao = new OrderMongodbDao("mongodb+srv://foodie:ccOUvdosBLzDprGM@foodie.cli5iha.mongodb.net/?retryWrites=true&w=majority");
    }

    //for test
    public OrderService(ActorSelection deliveryActor, ActorSelection restaurantActor,
                        OrderMongodbDao orderDao) {
        this.deliveryActor = deliveryActor;
        this.restaurantActor = restaurantActor;
        this.orderDao = orderDao;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CustomerOrderMessage.class, msg -> {
                    this.userActor = getSender();
                    System.out.println("******** Received order message: " + msg.getCustomer().getCustomerId());
                    // calculate price and store to db
                    OrderConfirmMessage orderConfirmMessage =
                            orderDao.insertCustomerOrderMessage(msg);
                    System.out.println("order id: " + orderConfirmMessage.getOrderId());
                    CustomerOrderMessage returnValue =
                            orderDao.selectByOrderId(orderConfirmMessage.getOrderId());
                    MessagePrinter.printCustomerOrderMessage(returnValue);

                    getSender().tell(orderConfirmMessage, getSelf());
                    ObjectMapper objectMapper = new ObjectMapper();
                    String jsonMessage = objectMapper.writeValueAsString("Received order, customer ID: " + msg.getCustomer().getCustomerId()
                            + " order ID: " + orderConfirmMessage.getOrderId());
                    sseController.sendMessageToClients(jsonMessage);
                })
                // .match(PaymentConfirmMessage.class, msg -> {
                .match(PaymentStatusMessage.class, msg -> {
                    System.out.println("******** Received payment message: ");
                    MessagePrinter.printPaymentConfirmMessage(msg);
                    ObjectMapper objectMapper = new ObjectMapper();
                    // change status in db
                    boolean updatePaymentStatus = orderDao.updatePaymentStatus(msg);
                    if (!updatePaymentStatus) {
                        System.out.println("Update payment status for orderID " + msg.getOrderId() +
                                " is not successful.");
                        String jsonMessage = objectMapper.writeValueAsString("Payment NOT successful for order ID: " + msg.getOrderId());
                        sseController.sendMessageToClients(jsonMessage);
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
                                order, msg.getOrderId());
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

                    String jsonMessage = objectMapper.writeValueAsString("Payment successful for order ID: " + msg.getOrderId());
                    sseController.sendMessageToClients(jsonMessage);
                })
                .match(DeliveryQueryMessage.class, msg -> {
                    userActor.tell(msg, getSelf());
                })
                .build();
    }

}
