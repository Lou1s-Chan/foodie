package ie.foodie.messages;

public class RestaurantQueryMessage {
    public enum QueryType {
        RESTAURANT_LIST,
        MENU_REQUEST
    }

    private final QueryType queryType;
    private final Integer restaurantId; // this can be null, as id = 0 might be possible

    public RestaurantQueryMessage(QueryType queryType) {
        this.queryType = queryType;
        this.restaurantId = null;
    }

    public RestaurantQueryMessage(QueryType queryType, int restaurantId) {
        this.queryType = queryType;
        this.restaurantId = restaurantId;
    }

    public QueryType getQueryType(){
        return queryType;
    }

    public Integer getRestaurantID(){
        return restaurantId;
    }
}
