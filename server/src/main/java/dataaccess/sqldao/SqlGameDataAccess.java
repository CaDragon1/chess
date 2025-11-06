package dataaccess.sqldao;

import dataaccess.DataAccessException;
import dataaccess.interfaces.GameDataAccess;
import models.GameData;
import server.ServerException;

import java.util.Collection;
import java.util.List;

public class SqlGameDataAccess implements GameDataAccess, SqlAccess {
    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public void createGame(GameData gameData) throws DataAccessException {

    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {

    }

    @Override
    public int executeUpdate(String statement, Object... params) throws ServerException {
        return 0;
    }

    @Override
    public void configureDatabase() throws ServerException {

    }
}
