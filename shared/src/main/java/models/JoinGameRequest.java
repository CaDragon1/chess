package models;

import chess.ChessGame;

public record JoinGameRequest(String authData, ChessGame.TeamColor teamColor, int gameID) {
}
