package server.handlers;

/**
 * CreateGameRequest is a record that could easily just be replaced by a gamename string except for the fact that
 * it makes deserialization easier.
 * @param gameName
 */
public record CreateGameRequest(String gameName) {
}
