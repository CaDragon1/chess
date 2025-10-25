package dataaccess;

import models.GameData;
import java.util.Collection;

public interface GameDataAccess {
    Collection<GameData> listGames() throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    void createGame(GameData gameData) throws DataAccessException;

    void updateGame(GameData gameData) throws DataAccessException;

    void clear() throws DataAccessException;
}
