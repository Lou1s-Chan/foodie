package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Login {
    // Method for user authentication
    public boolean login(String inputUsername, String inputPassword) {

        // Hardcoded user credentials (replace this with your actual authentication
        // logic)
        // String validUsername = "exampleUser";
        // String validPassword = "password123";
        String filePath = "userDB.txt";

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");
                int userId = Integer.parseInt(fields[0]);
                String username = fields[1];
                String email = fields[2];
                String password = fields[3];
                String address = fields[4];

                if (inputUsername.equals(username) && inputPassword.equals(password)) {
                    System.out.println("Login successful!");
                    return true;
                } else {
                    System.out.println("Login failed. Invalid username or password.");
                    return false;
                }
            }catch (IOException e) {
            e.printStackTrace();
            }
        }


        // Check if the provided username and password match the valid credentials
        // if (inputUsername.equals(validUsername) && inputPassword.equals(validPassword)) {
        //     System.out.println("Login successful!");
        //     return true;
        // } else {
        //     System.out.println("Login failed. Invalid username or password.");
        //     return false;
        // }
    }
}
