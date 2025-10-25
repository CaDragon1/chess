package dataaccess;

import models.UserData;

public interface UserDataAccess {
    UserData getUser(String username) throws DataAccessException;

    void addUser(UserData userData) throws  DataAccessException;

    void deleteUser(UserData userData) throws DataAccessException;

    void clear() throws DataAccessException;
}
