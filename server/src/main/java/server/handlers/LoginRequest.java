package server.handlers;

/**
 * LoginRequest is a record class to hold a username and password.
 * @param username
 * @param password
 */
public record LoginRequest(String username, String password) {
}
