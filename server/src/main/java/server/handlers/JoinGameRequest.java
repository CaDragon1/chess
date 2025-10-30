package server.handlers;

import chess.ChessGame;

public record JoinGameRequest(ChessGame.TeamColor team, int gameID) {
}
