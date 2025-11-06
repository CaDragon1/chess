package dataaccess.sqldao;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDataAccess;
import models.AuthData;
import server.ServerException;

public class SqlAuthDataAccess implements AuthDataAccess, SqlAccess {
    @Override
    public void createAuthData(AuthData authData) throws DataAccessException {

    }

    @Override
    public void deleteAuthData(String authData) throws DataAccessException {

    }

    @Override
    public AuthData getAuthData(String authData) throws DataAccessException {
        return null;
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
