package dataaccess;

import models.UserData;
import server.ServerException;
import java.sql.DriverManager;

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
CREATE TABLE IF NOT EXISTS  userdata (
'username' varChar(256) NOT NULL,
'password' varChar(256) NOT NULL,
'email' varChar(256) NOT NULL,
PRIMARY KEY 'username',

"""
    }

    @Override
    public int executeUpdate(String statement, Object... params) throws ServerException {
        return 0;
    }

    @Override
    public void configureDatabase() throws ServerException {
        try (var conn = DriverManager.getConnection())

    }
}
