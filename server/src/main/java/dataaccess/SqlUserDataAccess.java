package dataaccess;

import models.UserData;

import java.sql.SQLException;

public class SqlUserDataAccess implements UserDataAccess, SqlAccess {
    @Override
    public UserData getUserData(String username) {
        return null;
    }

    @Override
    public void addUserData(UserData userData) {

    }

    @Override
    public void clearUsers() {

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
    public int executeUpdate(String statement, Object... params) throws server.ServerException {
        return 0;
    }

    @Override
    public void configureDatabase() throws ServerException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {

        } catch (SQLException e) {
            throw new ServerException(e.getMessage());
        }

    }
}
