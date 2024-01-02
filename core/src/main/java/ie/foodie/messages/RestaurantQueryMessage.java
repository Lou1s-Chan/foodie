package ie.foodie.messages;

import akka.actor.ActorRef;

public class RestaurantQueryMessage implements MessageSerializable {
    public enum QueryType {
        RESTAURANT_LIST,
        MENU_REQUEST
    }

    private ActorRef userRef;
    private final QueryType queryType;
    private final Integer restaurantId; // this can be null, as id = 0 might be possible

    public RestaurantQueryMessage(QueryType queryType, ActorRef userRef) {
        this.queryType = queryType;
        this.userRef = userRef;
        this.restaurantId = null;
    }

    public RestaurantQueryMessage() {
        this.queryType = null;
        this.restaurantId = null;
    }

    public RestaurantQueryMessage(QueryType queryType, int restaurantId, ActorRef userRef) {
        this.queryType = queryType;
        this.restaurantId = restaurantId;
        this.userRef = userRef;
    }

    public QueryType getQueryType() {
        return queryType;
    }

    public Integer getRestaurantID() {
        return restaurantId;
    }

    public ActorRef getUserRef() {
        return userRef;
    }

    public void setUserRef(ActorRef userRef) {
        this.userRef = userRef;
    }
}
