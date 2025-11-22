package dataaccess.memorydao;

import dataaccess.DataAccessException;
import dataaccess.interfaces.GameDataAccess;
import models.GameData;

import java.util.ArrayList;
import java.util.List;

public class MemoryGameDataAccess implements GameDataAccess {
    List<GameData> gameDB;

    public MemoryGameDataAccess() {
        gameDB = new ArrayList<>();
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return gameDB;
    }

    @Override
    public GameData getGame(int gameID) {
        for (GameData game : gameDB) {
            if(game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException {
        gameDB.add(gameData);
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {
        boolean gameExists = false;
        for (GameData game : gameDB) {
            if(game.gameID() == gameData.gameID()) {
                gameDB.remove(game);
                gameDB.add(gameData);
                gameExists = true;
                break;
            }
        }
        if (!gameExists) {
            throw new DataAccessException("Game with the id [" + gameData.gameID() + "] was not found");
        }
    }

    @Override
    public void clear() throws DataAccessException {
        gameDB.clear();
    }
}
