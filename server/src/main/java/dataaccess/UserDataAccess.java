package dataaccess;

import models.UserData;

public interface UserDataAccess {
    /**
     * UserData methods
     */
    UserData getUserData(String username) throws ServerException;

    void addUserData(UserData userData) throws ServerException;

    /**
     * Mass deletion methods
     */
    void clearUsers() throws ServerException;
}
