package dataaccess.memorydao;

import dataaccess.DataAccessException;
import dataaccess.interfaces.GameDataAccess;
import models.GameData;

import java.util.Collection;
import java.util.HashSet;

public class MemoryGameDataAccess implements GameDataAccess {
    Collection<GameData> gameDB;

    public MemoryGameDataAccess() {
        gameDB = new HashSet<GameData>();
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return gameDB;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
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
        System.out.println("Attempting game update");
        boolean gameExists = false;
        for (GameData game : gameDB) {
            if(game.gameID() == gameData.gameID()) {
                System.out.println("Updating game...");
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
