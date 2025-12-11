package dataaccess;

import dataaccess.interfaces.UserDataAccess;
import models.UserData;

import java.sql.SQLException;

public class SqlUserDataAccess extends SqlDataAccess implements UserDataAccess {

    public SqlUserDataAccess () {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Getter for the username that prepares a statement, fetches the username from the database, and returns the userdata
     * @param username is the username we're trying to obtain data for
     * @return userdata associated with that username
     * @throws DataAccessException if there is an error getting the data
     */
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

    /**
     * Prepares sql statement to delete user data, then passes it
     * @throws DataAccessException if there is a failure
     */
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

    /**
     * SQL command to create the necessary table
     */
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
    protected String[] getCreateStatements() {
        return createStatements;  // Your existing private final String[] createStatements
    }
}
