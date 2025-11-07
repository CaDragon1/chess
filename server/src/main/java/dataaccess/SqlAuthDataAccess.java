package dataaccess;

import dataaccess.interfaces.AuthDataAccess;
import models.AuthData;
import server.ServerException;

import javax.xml.crypto.Data;
import java.sql.SQLException;

public class SqlAuthDataAccess implements AuthDataAccess, SqlAccess {

    public SqlAuthDataAccess () {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createAuthData(AuthData authData) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String insert = "INSERT INTO AuthData (authToken, username) VALUES (?, ?)";

            try (var preparedStatement = connection.prepareStatement(insert)) {
                preparedStatement.setString(1, authData.authToken());
                preparedStatement.setString(2, authData.username());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException("CreateAuthdata failure: " + e.getMessage());
        }
    }

    @Override
    public void deleteAuthData(String authData) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String remove = "DELETE FROM AuthData WHERE authToken = ?";

            try (var preparedStatement = connection.prepareStatement(remove)) {
                preparedStatement.setString(1, authData);
                // executeUpdate is nifty because it returns an integer that tells us how many rows were affected
                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected == 0) {
                    throw new DataAccessException("Auth token not found");
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Delete Authdata failure: " + e.getMessage());
        }

    }

    @Override
    public AuthData getAuthData(String authData) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String fetch = "SELECT * FROM AuthData WHERE authToken = ?";

            try (var preparedStatement = connection.prepareStatement(fetch)) {
                preparedStatement.setString(1, authData);

                try (var response = preparedStatement.executeQuery()) {
                    if (!response.next()) {
                        throw new DataAccessException("AuthToken not found");
                    }
                    return new AuthData(response.getString("authToken"),
                            response.getString("username"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Get Authdata failure: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            var clear = "DELETE FROM AuthData";

            try (var preparedStatement = connection.prepareStatement(clear)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Authdata clear failure: " + e.getMessage());
        }
    }

    @Override
    public int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
                return preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Execute Update failure: " + e.getMessage());
        }
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
    public void configureDatabase() throws DataAccessException {
        try {
            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }
}
