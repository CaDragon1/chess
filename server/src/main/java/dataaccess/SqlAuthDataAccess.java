package dataaccess;

import models.AuthTokenData;
import server.ServerException;

public class SqlAuthDataAccess implements AuthDataAccess, SqlAccess {
    @Override
    public void addAuthData(AuthTokenData authData) {

    }

    @Override
    public void removeAuthData(AuthTokenData authData) {

    }

    @Override
    public AuthTokenData getAuthData(String authData) {
        return null;
    }

    @Override
    public void clearAuthTokens() {

    }

    @Override
    public int executeUpdate(String statement, Object... params) throws ServerException {
        return 0;
    }

    @Override
    public void configureDatabase() throws ServerException {

    }
}
