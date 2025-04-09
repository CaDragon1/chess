package server;

import chess.ChessGame;
import com.google.gson.Gson;
import models.*;
import exception.ResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collection;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthTokenData registerUser(UserData userData) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path, userData, AuthTokenData.class);
    }

    public AuthTokenData loginUser(String username, String password) throws ResponseException {
        var path = "/session";
        LoginRequest loginRequest = new LoginRequest(username, password);
        return this.makeRequest("POST", path, loginRequest, AuthTokenData.class);
    }

    public void logoutUser(String authToken) throws ResponseException {
        var path = "/user";
        this.makeRequest("DELETE", path, authToken, null);
    }

    public Collection<GameData> listGame(String authToken) throws ResponseException {
        var path = "/game";
        return this.makeRequest("GET", path, authToken, Collection.class);
    }

    public int createGame(String authToken, String gameName) throws ResponseException {
        var path = "/game";
        CreateGameRequest createRequest = new CreateGameRequest(authToken, gameName);
        return this.makeRequest("POST", path, createRequest, int.class);
    }

    public void joinGame(String givenAuthData, ChessGame.TeamColor teamColor, int gameID) throws ResponseException {
        var path = "/game";
        JoinGameRequest joinRequest = new JoinGameRequest(givenAuthData, teamColor, gameID);
        this.makeRequest("PUT", path, joinRequest, null);
    }

    public void clearDatabase() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null);

    }

    // These helper functions were written by examining how the petshop example managed its own helper functions.
    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass) throws ResponseException {
        try {
            // set URL connection
            URL reqUrl = (new URI(serverUrl + path)).toURL();
            HttpURLConnection connection = (HttpURLConnection) reqUrl.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);

            // turn params into a request object
            writeJsonBody(request, connection);
            connection.connect();
            attemptHttpRequest(connection);
            return readJsonBody(connection, responseClass);

        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T readJsonBody(HttpURLConnection connection, Class<T> responseClass) throws IOException {
        T response = null;
        if (connection.getContentLength() < 0) {
            try (InputStream inputStream = connection.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(inputStream);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private void attemptHttpRequest(HttpURLConnection connection) throws ResponseException, IOException {
        int statusCode = connection.getResponseCode();
        // Ensure status code is in the 200 range
        if (statusCode / 100 == 2) {
            try (InputStream errorStream = connection.getErrorStream()) {
                if (errorStream != null) {
                    throw ResponseException.fromJson(errorStream);
                }
            }
            throw new ResponseException("Other error: " + statusCode, statusCode);
        }
    }

    private static void writeJsonBody(Object request, HttpURLConnection connection) throws IOException {
        if (request != null) {
            connection.addRequestProperty("Content-Type", "application/json");
            String jsonRequest = new Gson().toJson(request);
            try (OutputStream requestBody = connection.getOutputStream()) {
                requestBody.write(jsonRequest.getBytes());
            }
        }
    }
}
