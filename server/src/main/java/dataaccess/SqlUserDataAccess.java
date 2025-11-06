package dataaccess;

import dataaccess.interfaces.UserDataAccess;
import models.UserData;
import server.ServerException;

import java.sql.SQLException;

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

    // SQL command to create the necessary table
    private final String[] createStatements = {
            """
                CREATE TABLE IF NOT EXISTS  UserData (
                `username` varChar(256) NOT NULL PRIMARY KEY,
                `password` varChar(60) NOT NULL,
                `email` varChar(256) NOT NULL UNIQUE
                )
            """
    };

    @Override
    public int executeUpdate(String statement, Object... params) throws ServerException {
        return 0;
    }

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
