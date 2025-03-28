package dataaccess;

import models.AuthTokenData;
import models.UserData;

import java.sql.SQLException;

public class SqlUserDataAccess implements UserDataAccess, SqlAccess {
    @Override
    public UserData getUserData(String username) throws ServerException {
        try (var conn = DatabaseManager.getConnection()) {
            var fetch = "SELECT * FROM UserData WHERE username = ?";

            try (var preparedStatement = conn.prepareStatement(fetch)) {
                preparedStatement.setString(1, username);

                try (var response = preparedStatement.executeQuery()) {
                    if (response.next()) {
                        return new UserData(response.getString("username"),
                                response.getString("password"),
                                response.getString("email"));
                    }
                }
            }
        } catch (SQLException e) {
            throw new ServerException("Userdata get failed: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void addUserData(UserData userData) throws ServerException {
        try (var conn = DatabaseManager.getConnection()) {
            var insert = "INSERT INTO UserData (username, password, email) VALUES (?, ?, ?)";

            try (var preparedStatement = conn.prepareStatement(insert)) {
                preparedStatement.setString(1, userData.username());
                preparedStatement.setString(2, userData.password());
                preparedStatement.setString(3, userData.email());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ServerException("Userdata add failed: " + e.getMessage());
        }
    }

    @Override
    public void clearUsers() throws ServerException {
        try (var conn = DatabaseManager.getConnection()) {
            var clear = "DELETE TABLE UserData";

            try (var preparedStatement = conn.prepareStatement(clear)) {
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ServerException("UserData clear failed: " + e.getMessage());
        }
    }

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
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(statement)) {
                for (int i = 0; i < params.length; i++) {
                    preparedStatement.setObject(i + 1, params[i]);
                }
                return preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new ServerException("Update failed: " + e.getMessage());
        }
    }

    @Override
    public void configureDatabase() throws ServerException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e.getMessage());
        }

    }
}
