package service;

import Models.AuthTokenData;
import Models.GameData;
import Models.UserData;
import chess.ChessGame;
import dataaccess.*;
import server.Server;
import server.ServerException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;

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
        throw new ServerException("unauthorized", 401);
    }

    /**
     * Log out an existing user from the database
     * @param authToken is the current login session's authToken
     * @throws ServerException 401
     */
    public void logOut(String authToken) throws ServerException {
        AuthTokenData authData = authDataAccess.getAuthData(authToken);
        if (authData != null) {
            authDataAccess.removeAuthData(authData);
        }
        throw new ServerException("unauthorized", 401);
    }

    /**
     * List all games currently in the database
     * @param authToken is the user's current login session's authToken
     * @return the list of all games
     * @throws ServerException 401
     */
    public Collection<GameData> listGames(String authToken) throws ServerException {
        AuthTokenData authData = authDataAccess.getAuthData(authToken);
        if (authData != null) {
            return gameDataAccess.getGameList();
        }
        throw new ServerException("unauthorized", 401);
    }

    /**
     * Create a new game if the existing game name doesn't exist
     * @param authToken is the user's current login session's stored-in-the-database's authToken
     * @param gameName
     * @return
     * @throws ServerException
     */
    public int createGame(String authToken, String gameName) throws ServerException {
        AuthTokenData authData = authDataAccess.getAuthData(authToken);
        if (authData != null) {
            if (gameDataAccess.getGameByName(gameName) == null) {
                ChessGame newGame = new ChessGame();
                int gameID = generateGameID();
                GameData newGameData = new GameData(gameID, null, null, gameName, newGame);
                return gameID;
            }
            throw new ServerException("already taken", 403);
        }
        throw new ServerException("unauthorized", 401);
    }

    /**
     * The following are functions to generate IDs for our application.
     */
    // The general implementation for this function came from
    // https://stackoverflow.com/questions/13992972/how-to-create-an-authentication-token-using-java
    // I don't recall us talking about how to do this ourselves, so I used this implementation.
    private String generateAuthToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        String authToken = encoder.encodeToString(randomBytes);

        // Verify uniqueness
        if (authDataAccess.getAuthData(authToken) != null) {
            return generateAuthToken();
        }
        return authToken;
    }

    private int generateGameID() {
        byte[] randomBytes = new byte[4];
        secureRandom.nextBytes(randomBytes);
        // Turn bytes into integer
        int gameID = java.nio.ByteBuffer.wrap(randomBytes).getInt();

        // Verify uniqueness
        while (gameDataAccess.getGameByID(gameID) != null) {
            secureRandom.nextBytes(randomBytes);
            gameID = java.nio.ByteBuffer.wrap(randomBytes).getInt();
        }
        return gameID;
    }

}