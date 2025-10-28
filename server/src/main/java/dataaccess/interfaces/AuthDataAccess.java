package dataaccess.interfaces;

import dataaccess.DataAccessException;
import models.AuthData;

public interface AuthDataAccess {
    void createAuthData(AuthData authData) throws DataAccessException;

    void deleteAuthData(String authData) throws DataAccessException;

    AuthData getAuthData(String authData) throws DataAccessException;

    void clear() throws DataAccessException;
}
