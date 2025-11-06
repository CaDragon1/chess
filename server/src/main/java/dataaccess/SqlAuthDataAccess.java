package dataaccess;

import dataaccess.interfaces.AuthDataAccess;
import models.AuthData;
import server.ServerException;

import java.sql.SQLException;

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

    private final String[] createStatements = {
            """
                CREATE TABLE IF NOT EXISTS AuthData (
                `authToken` varChar(64) NOT NULL PRIMARY KEY,
                `username` varChar(256) NOT NULL
                )
            """
    };

    @Override
    public void configureDatabase() throws ServerException {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new ServerException(e.getMessage(), 500);
        }
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new ServerException(e.getMessage(), 500);
        }
    }
}
