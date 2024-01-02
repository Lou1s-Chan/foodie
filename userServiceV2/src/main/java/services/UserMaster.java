package services;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import ie.foodie.actors.FoodieActor;
import ie.foodie.messages.RestaurantQueryMessage;
import ie.foodie.messages.models.CustomerV2;

public class UserMaster extends FoodieActor {
    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(CustomerV2.class, customerV2 -> {
                    String uuid = customerV2.getUuid();
                    String slaveUser = "user-service-slave-" + uuid;
                    getContext().getSystem().actorOf(UserSlave.props(), slaveUser);
                    ActorRef userRef = getContext().child(slaveUser).get();
                    ActorSelection orderSystem = getContext().getSystem().actorSelection("akka.tcp://order-system@localhost:2553/user/order-service");
                    orderSystem.tell(new RestaurantQueryMessage(RestaurantQueryMessage.QueryType.RESTAURANT_LIST, userRef), userRef);
                })
                .match(String.class, userUuid -> {
                    ActorRef userRef = getContext().child("user-service-slave-" + userUuid).get();
                    context().stop(userRef);
                })
                .build();
    }

    public static Props props() {
        return Props.create(UserMaster.class);
    }
}
