package service;

import Models.AuthTokenData;
import Models.UserData;
import dataaccess.*;
import server.ServerException;

import java.security.SecureRandom;
import java.util.Base64;

public class Service {
    UserDataAccess userDataAccess;
    AuthDataAccess authDataAccess;
    GameDataAccess gameDataAccess;
    AuthTokenData authTokenData;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();

    public Service() {
        userDataAccess = new MemoryUserDataAccess();
        authDataAccess = new MemoryAuthDataAccess();
        gameDataAccess = new MemoryGameDataAccess();
    }


    /**
     * ChessService to register a user in the database
     * @param userData is the UserData object containing the user's data
     * @return the AuthTokenData object created upon registration and logging in to the system
     * @throws ServerException 403: name already taken
     */
    public AuthTokenData register(UserData userData) throws ServerException {
        if (userDataAccess.getUserData(userData.username()) == null) {

            userDataAccess.addUserData(userData);

            authTokenData = new AuthTokenData(generateAuthToken(), userData.username());

            authDataAccess.addAuthData(authTokenData);

            return authTokenData;
        }
        else {
            throw new ServerException("already taken", 403);
        }
    }

    /**
     * Log in a user into the database
     * @param username is the user's username
     * @param password is the user's password
     * @return the AuthTokenData object created upon login
     * @throws ServerException 401
     */
    public AuthTokenData login(String username, String password) throws ServerException {
        UserData userData = userDataAccess.getUserData(username);
        if (userData != null) {
            if(userData.password().equals(password)) {
                authTokenData = new AuthTokenData(generateAuthToken(), username);
                authDataAccess.addAuthData(authTokenData);

                return authTokenData;
            }
        }
        throw new ServerException("bad request", 401);
    }

    public AuthTokenData logOut(String authToken) throws ServerException {
        AuthTokenData authData = authDataAccess.getAuthData(authToken);
        if (authData != null) {
            authDataAccess.removeAuthData(authData);
            return authData;
        }
        throw new ServerException("unauthorized", 401);
    }

    // The implementation for this function came from
    // https://stackoverflow.com/questions/13992972/how-to-create-an-authentication-token-using-java
    // I don't recall us talking about how to do this ourselves, so I used this implementation.
    private String generateAuthToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return encoder.encodeToString(randomBytes);
    }

}