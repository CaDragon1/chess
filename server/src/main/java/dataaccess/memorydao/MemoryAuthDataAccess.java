package dataaccess.memorydao;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDataAccess;
import models.AuthData;
import models.UserData;

import java.util.Collection;
import java.util.HashSet;

public class MemoryAuthDataAccess implements AuthDataAccess {
    Collection<AuthData> authTokenDB;

    public MemoryAuthDataAccess() {
        authTokenDB = new HashSet<AuthData>();
    }

    @Override
    public void createAuthData(AuthData authData) throws DataAccessException {
        authTokenDB.add(authData);
    }

    // New method for single session verification, not in the .md but needed
    public AuthData getCurrentUserAuthToken(String username) {
        for (AuthData token : authTokenDB) {
            if (token.username().equals(username)) {
                return token;
            }
        }
        return null;
    }

    @Override
    public void deleteAuthData(String authData) throws DataAccessException {
        boolean foundToken = false;
        for (AuthData token : authTokenDB) {
            if (token.authToken().equals(authData)) {
                authTokenDB.remove(token);
                foundToken = true;
                break;
            }
        }
        if (!foundToken) {
            throw new DataAccessException("unauthorized");
        }
    }

    @Override
    public AuthData getAuthData(String authData) throws DataAccessException {
        for (AuthData token : authTokenDB) {
            if (token.authToken().equals(authData)) {
                return token;
            }
        }
        throw new DataAccessException("unauthorized");
    }

    @Override
    public void clear() throws DataAccessException {
        authTokenDB.clear();
    }
}
