package dataaccess.sqldao;

import dataaccess.DataAccessException;
import dataaccess.interfaces.UserDataAccess;
import models.UserData;
import server.ServerException;

public class SqlUserDataAccess implements UserDataAccess, SqlAccess {
    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void addUser(UserData userData) throws DataAccessException {

    }

    @Override
    public void deleteUser(UserData userData) throws DataAccessException {

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
