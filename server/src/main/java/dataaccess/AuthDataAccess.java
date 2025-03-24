package dataaccess;

import models.AuthTokenData;

public interface AuthDataAccess {
    /**
     * AuthData methods
     */
    void addAuthData(AuthTokenData authData) throws ServerException;

    void removeAuthData(AuthTokenData authData) throws ServerException;

    AuthTokenData getAuthData(String authData) throws ServerException;

    /**
     * Mass deletion methods
     */
    void clearAuthTokens() throws ServerException;
}
