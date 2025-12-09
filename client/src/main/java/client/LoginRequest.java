package client;

/**
 * Record containing the information for a login request; here for json serialization
 * @param username
 * @param password
 */
public record LoginRequest(String username, String password) {
}
