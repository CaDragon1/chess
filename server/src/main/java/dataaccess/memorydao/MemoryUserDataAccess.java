package dataaccess.memorydao;

import dataaccess.DataAccessException;
import dataaccess.interfaces.UserDataAccess;
import models.UserData;

import java.util.Collection;
import java.util.HashSet;

public class MemoryUserDataAccess implements UserDataAccess {

    Collection<UserData> userDB;

    public MemoryUserDataAccess() {
        userDB = new HashSet<UserData>();
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        for (UserData user : userDB) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }

    @Override
    public void addUser(UserData userData) throws DataAccessException {
        userDB.add(userData);
    }

    @Override
    public void clear() throws DataAccessException {
        userDB.clear();
    }
}
