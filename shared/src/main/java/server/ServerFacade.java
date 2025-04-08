package server;

import models.AuthTokenData;
import models.UserData;
import exception.ResponseException;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthTokenData registerUser(UserData userData) {

        var path = "/user";
        return this.makeRequest("POST", );
    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {

    }
}
