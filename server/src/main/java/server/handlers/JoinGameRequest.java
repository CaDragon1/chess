package server.handlers;

/**
 * JoinGameRequest is a record containing the necessary parameters to join a game.
 * @param playerColor
 * @param gameID
 */
public record JoinGameRequest(String playerColor, int gameID) {
}
