package ie.foodie;

import ie.foodie.database.OrderDao;

public class Main {
    public static void main(String[] args) {

        OrderDao.connect();

        System.out.println("Hello world!");
    }
}