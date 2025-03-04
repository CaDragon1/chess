package service;

import Models.AuthTokenData;
import Models.UserData;
import dataaccess.DataAccess;
import org.eclipse.jetty.server.Authentication;
import server.ServerException;

import java.security.SecureRandom;
import java.util.Base64;

public class Service {
    DataAccess dataAccess;
    AuthTokenData authToken;
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();

    public Service() {
        dataAccess = new DataAccess();
    }


    public AuthTokenData register(UserData userData) throws ServerException {
        if (dataAccess.getUserData(userData.username()) == null) {
            dataAccess.addUserData(userData);

            authToken = new AuthTokenData(generateAuthToken(), userData.username());

            dataAccess.addAuthData(authToken);

            return authToken;
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
