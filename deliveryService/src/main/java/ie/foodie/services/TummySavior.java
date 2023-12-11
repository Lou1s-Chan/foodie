package ie.foodie.services;

public class TummySavior {
    public boolean tummySaviorDelivered(){
        try {
            //Assumed order delivered after 20 seconds here.
            Thread.sleep(20);
            return true;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
