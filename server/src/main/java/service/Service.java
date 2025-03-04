package service;

import Models.AuthTokenData;
import Models.UserData;
import dataaccess.DataAccess;
import server.ServerException;

import java.security.SecureRandom;
import java.util.Base64;

public class Service {
    DataAccess dataAccess;
    AuthTokenData authTokenData;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();

    public Service() {
        dataAccess = new DataAccess();
    }


    /**
     * Service to register a user in the database
     * @param userData is the UserData object containing the user's data
     * @return the AuthTokenData object created upon registration and logging in to the system
     * @throws ServerException 403: name already taken
     */
    public AuthTokenData register(UserData userData) throws ServerException {
        if (dataAccess.getUserData(userData.username()) == null) {
            dataAccess.addUserData(userData);

            authTokenData = new AuthTokenData(generateAuthToken(), userData.username());

            dataAccess.addAuthData(authTokenData);

            return authTokenData;
        }
        else {
            throw new ServerException("already taken", 403);
        }
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
