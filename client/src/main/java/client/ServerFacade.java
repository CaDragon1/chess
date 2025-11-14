package client;

import chess.ChessGame;
import com.google.gson.Gson;
import models.*;
import exception.ResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.Collection;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData registerUser(UserData userData) throws ResponseException {

    }

    public AuthData loginUser(String username, String password) throws ResponseException {

    }

    public void logoutUser(String authToken) throws ResponseException {

    }

    public Collection<GameData> listGame(String authToken) throws ResponseException {

    }

    public int createGame(String authToken, String gameName) throws ResponseException {

    }

    public void joinGame(String givenAuthData, ChessGame.TeamColor teamColor, int gameID) throws ResponseException {

    }

    public void clearDatabase() throws ResponseException {

    }

    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws ResponseException {

    }

    private <T> T readJsonBody(HttpURLConnection connection, Class<T> responseClass) throws IOException {

    }

    private void attemptHttpRequest(HttpURLConnection connection) throws ResponseException, IOException {

    }

    private static void writeJsonBody(Object request, HttpURLConnection connection) throws IOException {

    }
}