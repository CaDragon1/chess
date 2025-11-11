package dataaccess;

import dataaccess.interfaces.UserDataAccess;
import models.UserData;

import java.sql.SQLException;

public class SqlUserDataAccess implements UserDataAccess, SqlAccess {

    public SqlUserDataAccess () {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String fetch = "SELECT * FROM UserData WHERE username = ?";

            UserData response = null;

            try (var preparedStatement = connection.prepareStatement(fetch)) {
                preparedStatement.setString(1, username);

                try (var resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        response = new UserData(resultSet.getString("username"),
                                resultSet.getString("password"),
                                resultSet.getString("email"));
                    }
                    else {
                        throw new SQLException("User not found");
                    }
                }
            }

            if (response.username() != null && response.password() != null && response.email() != null) {
                return response;
            }
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    @Override
    public void addUser(UserData userData) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String insert = "INSERT INTO UserData (username, password, email) VALUES (?, ?, ?)";

            try (var preparedStatement = connection.prepareStatement(insert)) {
                preparedStatement.setString(1, userData.username());
                preparedStatement.setString(2, userData.password());
                preparedStatement.setString(3, userData.email());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Add user failure: " + e.getMessage());
        }
    }

    @Override
    public void deleteUser(UserData userData) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String delete = "DELETE FROM UserData WHERE username = ?";

            try (var preparedStatement = connection.prepareStatement(delete)) {
                preparedStatement.setString(1, userData.username());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Delete user failure: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            String clear = "DELETE FROM UserData";

            try (var preparedStatement = connection.prepareStatement(clear)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Delete user failure: " + e.getMessage());
        }
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
    public int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var connection = DatabaseManager.getConnection()) {
            try (var preparedStatement = connection.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
                return preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("User DAO update failure: " + e.getMessage());
        }
    }

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
