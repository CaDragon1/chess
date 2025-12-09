package client;

import chess.ChessGame;
import chess.ChessMove;
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
import java.util.List;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    /**
     * registers a user through a post request
     * @param userData is the userdata used for registration
     * @return authData object upon successful registration
     * @throws ResponseException if post request fails
     */
    public AuthData registerUser(UserData userData) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path, userData, AuthData.class, null);
    }

    /**
     * logs in a user through a post request
     * @param username is the username used for login
     * @param password is the password used for login
     * @return authData object upon successful login
     * @throws ResponseException if post request fails
     */
    public AuthData loginUser(String username, String password) throws ResponseException {
        var path = "/session";
        LoginRequest loginRequest = new LoginRequest(username, password);
        return this.makeRequest("POST", path, loginRequest, AuthData.class, null);
    }

    /**
     * logs out a user through a delete request
     * @param authToken is the user authtoken we want to use for logout
     * @throws ResponseException if delete request fails
     */
    public void logoutUser(String authToken) throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null, authToken);
    }

    /**
     * method to use a get request to obtain a game list
     * @param authToken contains authorization for the request
     * @return a list of gamedata objects
     * @throws ResponseException if request fails
     */
    public List<GameData> listGame(String authToken) throws ResponseException {
        var path = "/game";
        // Record for the game collection
        record ListedGames(List<GameData> games) {}
        // Convert return into GameData collection
        return this.makeRequest("GET", path, null, ListedGames.class, authToken).games;
    }

    /**
     * creates a new game on the server through a post request
     * @param authToken is the authorization token for the current user
     * @param gameName is the name of the game to be created
     * @return the unique game ID assigned by the server
     * @throws ResponseException if the post request fails
     */
    public int createGame(String authToken, String gameName) throws ResponseException {
        var path = "/game";
        var requestBody = Map.of("gameName", gameName);
        // Record for int response conversion
        record createdGame(int gameID) {}
        return this.makeRequest("POST", path, requestBody, createdGame.class, authToken).gameID;
    }

    /**
     * joins an existing game on the server through a put request
     * @param givenAuthData is the authorization token of the user joining the game
     * @param teamColor is the desired team color for the user in the game
     * @param gameID is the unique identifier of the game to join
     * @throws ResponseException if the put request fails
     */
    public void joinGame(String givenAuthData, ChessGame.TeamColor teamColor, int gameID) throws ResponseException {
        var path = "/game";
        var requestBody = Map.of("playerColor", teamColor, "gameID", gameID);
        this.makeRequest("PUT", path, requestBody, null, givenAuthData);
    }

    /**
     * clears all server-side data including users, games, and sessions
     * @throws ResponseException if the delete request fails
     */
    public void clearDatabase() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws ResponseException {
        var path = "/game";
        var requestBody = Map.of("move", move, "gameID", gameID);
        this.makeRequest("POST", path, requestBody, null, authToken);
    }

    /**
     * helper method to perform an HTTP request and parse a JSON response
     * @param method is the HTTP method to use (e.g., GET, POST, PUT, DELETE)
     * @param path is the URL path on the server for the request
     * @param request is the request body object to be serialized as JSON, or null if none
     * @param responseClass is the class type to deserialize the JSON response into, or null if none
     * @param authToken is the authorization token to include in the request header, or null if not needed
     * @return a deserialized response object of type T, or null if responseClass is null
     * @throws ResponseException if the request fails or returns a non-2xx status code
     */
    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            // set URL connection
            URL reqUrl = (new URI(serverUrl + path)).toURL();
            HttpURLConnection connection = (HttpURLConnection) reqUrl.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);

            // Set authToken in header
            if (authToken != null) {
                connection.setRequestProperty("Authorization", authToken);
            }

            // turn params into a request object
            writeJsonBody(request, connection);
            connection.connect();
            attemptHttpRequest(connection);
            return readJsonBody(connection, responseClass);

        } catch (ResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseException("Connection error: " + e.getMessage(), 500);
        }
    }

    /**
     * helper method to read and deserialize a JSON response body
     * @param connection is the active HTTP connection containing the response
     * @param responseClass is the class type to deserialize the JSON body into, or null if no body is expected
     * @param <T> is the generic type of the response object
     * @return a deserialized response object of type T, or null if responseClass is null
     * @throws IOException if an I/O error occurs while reading the response
     */
    private <T> T readJsonBody(HttpURLConnection connection, Class<T> responseClass) throws IOException {
        if (responseClass == null) {
            return null;
        }
        try (InputStream inputStream = connection.getInputStream()) {
            InputStreamReader reader = new InputStreamReader(inputStream);
            return new Gson().fromJson(reader, responseClass);
        }
    }

    /**
     * helper method to validate the HTTP response status code and throw appropriate exceptions
     * @param connection is the active HTTP connection used for the request
     * @throws ResponseException if the status code is not in the 2xx range and an error body is present
     * @throws IOException if an I/O error occurs while reading the error stream
     */
    private void attemptHttpRequest(HttpURLConnection connection) throws ResponseException, IOException {
        int statusCode = connection.getResponseCode();
        // Ensure status code is in the 200 range. If it doesn't, we figure out what to throw.
        if (statusCode / 100 != 2) {
            try (InputStream errorStream = connection.getErrorStream()) {
                if (errorStream != null) {
                    throw ResponseException.fromJson(errorStream);
                }
            }
            throw new ResponseException("Other error: " + statusCode, statusCode);
        }
    }

    /**
     * helper method that serializes a request object to JSON and writes it to the HTTP request body
     * @param request is the object to be serialized as JSON, or null if no body is needed
     * @param connection is the active HTTP connection to which the body will be written
     * @throws IOException if an I/O error occurs while writing the request body
     */
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