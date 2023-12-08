package service;

public class UserInfo {
    // Attributes
    private int userId;
    private String username;
    private String email;
    private String password;
    private String address;

    // Constructors
    public UserInfo() {
        // Default constructor
    }

    public UserInfo(int userId, String username, String email, String password, String address) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getter and Setter methods
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Other methods (if needed)
    // ...

    // toString method for easy printing
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    // Example usage in main method

}
