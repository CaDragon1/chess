package dataaccess;

import chess.ChessGame;
import models.AuthTokenData;
import models.GameData;

import java.util.Collection;
import java.util.List;

public class SqlGameDataAccess implements GameDataAccess {
    @Override
    public Collection<GameData> getGameList() {
        return List.of();
    }

    @Override
    public GameData getGameByName(String gameName) {
        return null;
    }

    @Override
    public GameData getGameByID(int gameID) {
        return null;
    }

    @Override
    public void createGame(GameData gameData) {

    }

    @Override
    public void joinGame(AuthTokenData authData, ChessGame.TeamColor team, int gameID) {

    }

    @Override
    public void clearGames() {

    }
}
