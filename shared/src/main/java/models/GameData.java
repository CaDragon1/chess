package models;

import chess.ChessGame;
import com.google.gson.Gson;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game, GameStatus status) {

    public enum GameStatus {PREGAME, RESIGNED, LIVE, WHITE_WIN, BLACK_WIN, STALEMATE}

    public GameData setWhiteUsername(String whiteUsername) {
        return new GameData(this.gameID, whiteUsername, this.blackUsername, this.gameName, this.game, this.status);
    }

    public GameData setBlackUsername(String blackUsername) {
        return new GameData(this.gameID, this.whiteUsername, blackUsername, this.gameName, this.game, this.status);
    }

    public ChessGame getGame() {
        return game;
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}