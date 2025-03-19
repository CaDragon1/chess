package dataaccess;

import models.AuthTokenData;

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
    public int executeUpdate(String statement, Object... params) throws server.ServerException {
        return 0;
    }

    private final String[] createStatements = {
            """
                CREATE TABLE IF NOT EXISTS  AuthData (
                `authToken` INT NOT NULL PRIMARY KEY,
                `username` varChar(256) NOT NULL,
                FOREIGN KEY (username) REFERENCES UserData(username) ON DELETE SET NULL,
                )
            """
    };

    @Override
    public void configureDatabase() throws ServerException {

    }
}
