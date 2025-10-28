package dataaccess.memorydao;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDataAccess;
import models.AuthData;

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
        if (foundToken) {
            throw new DataAccessException("Auth Data not found");
        }
    }

    @Override
    public AuthData getAuthData(String authData) throws DataAccessException {
        for (AuthData token : authTokenDB) {
            if (token.authToken().equals(authData)) {
                return token;
            }
        }
        throw new DataAccessException("No authToken of requested id found");
    }

    @Override
    public void clear() throws DataAccessException {
        authTokenDB.clear();
    }
}
