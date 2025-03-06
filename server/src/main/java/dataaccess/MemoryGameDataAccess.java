package dataaccess;

import Models.AuthTokenData;
import Models.GameData;
import chess.ChessGame;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class MemoryGameDataAccess implements GameDataAccess {
    Collection<GameData> gameDatabase;

    public MemoryGameDataAccess() {
        gameDatabase = new HashSet<GameData>();
    }

    @Override
    public Collection<GameData> getGameList() {
        return gameDatabase;
    }

    @Override
    public GameData getGameByName(String gameName) {
        for (GameData game : gameDatabase) {
            if (game.gameName().equals(gameName)) {
                return game;
            }
        }
        return null;
    }

    @Override
    public GameData getGameByID(int gameID) {
        for (GameData game : gameDatabase) {
            if (game.gameID() == (gameID)) {
                return game;
            }
        }
        return null;
    }

    @Override
    public void createGame(GameData gameData) {
        gameDatabase.add(gameData);
    }

    @Override
    public void joinGame(AuthTokenData authData, ChessGame.TeamColor team, int gameID) {
        if (team == ChessGame.TeamColor.WHITE) {
            getGameByID(gameID).setWhiteUsername(authData.username());
        } else if (team == ChessGame.TeamColor.BLACK) {
            getGameByID(gameID).setBlackUsername(authData.username());
        }
    }

    @Override
    public void clearGames() {
        gameDatabase.clear();
    }
}
