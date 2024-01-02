package ie.foodie.messages;

import akka.actor.ActorRef;

import java.util.ArrayList;

public class RestaurantsResponse implements MessageSerializable {
    private final ArrayList<RestaurantData> restaurants;
    private ActorRef userRef;
    private String msgType;

    public RestaurantsResponse() {
        this.restaurants = new ArrayList<RestaurantData>();
    }

    public RestaurantsResponse(ArrayList<RestaurantData> restaurants, ActorRef userRef) {
        this.msgType = "RestaurantsResponse";
        this.restaurants = restaurants;
        this.userRef = userRef;
    }

    public ArrayList<RestaurantData> getRestaurants() {
        return restaurants;
    }

    public String getMsgType() {
        return msgType;
    }

    public ActorRef getUserRef() {
        return userRef;
    }

    public void setUserRef(ActorRef userRef) {
        this.userRef = userRef;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RestaurantsResponse{");
        for (RestaurantData restaurant : restaurants) {
            sb.append("\n\t").append(restaurant.toString());
        }
        sb.append("\n}");
        return sb.toString();
    }

    public static class RestaurantData implements MessageSerializable {
        private int id;
        private String name;
        private String address;
        private String description;
        private String website;

        public RestaurantData(int id, String name, String address, String description, String website) {
            this.id = id;
            this.name = name;
            this.address = address;
            this.description = description;
            this.website = website;
        }

        public RestaurantData(){

        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        public String getDescription() {
            return description;
        }

        public String getWebsite() {
            return website;
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setWebsite(String website) {
            this.website = website;
        }

        @Override
        public String toString() {
            return "RestaurantData{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", address='" + address + '\'' +
                    ", description='" + description + '\'' +
                    ", website='" + website + '\'' +
                    '}';
        }
    }
}
