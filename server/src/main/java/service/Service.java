package service;

import Models.AuthTokenData;
import Models.UserData;
import dataaccess.DataAccess;
import org.eclipse.jetty.server.Authentication;

public class Service {
    DataAccess dataAccess;
    AuthTokenData authToken;

    public Service() {
        dataAccess = new DataAccess();
    }


    public AuthTokenData register(UserData userData) {
        if (dataAccess.getUserData(userData.username()) == null) {
            dataAccess.addUserData(userData);

            authToken = new AuthTokenData("authTokenHere", userData.username());

            dataAccess.addAuthData(authToken);

            return authToken;
        }
        else {
            return null;
        }
    }
}
