package dataaccess.interfaces;

import dataaccess.DataAccessException;
import models.GameData;
import java.util.Collection;
import java.util.List;

public interface GameDataAccess {
    List<GameData> listGames() throws DataAccessException;

    GameData getGame(int gameID) throws DataAccessException;

    void createGame(GameData gameData) throws DataAccessException;

    void updateGame(GameData gameData) throws DataAccessException;

    void clear() throws DataAccessException;
}
