package server.handlers;

public class LoginRequest {
    private String username;
    private String password;

    // Constructor for Jackson which is part of Javalin
    public LoginRequest() {}
    
    public String getUsername() {
        return username;
    }
    public void setUsername(String name) {
        username = name;
    }
    public String getPassword() {
        return password;
    }
    private void setPassword(String pass) {
        password = pass;
    }
}
