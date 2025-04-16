package client;

import chess.ChessGame;
import dataaccess.AuthDataAccess;
import exception.ResponseException;
import models.AuthTokenData;
import models.GameData;
import models.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.Collection;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static int port;
    private ServerFacade serverFacade;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void serverInitialize() throws ResponseException {
        serverFacade = new ServerFacade("http://localhost:" + port);
        serverFacade.clearDatabase();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    @Order(1)
    public void registerUserSuccess() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);
        Assertions.assertNotNull(registerResult, "Should return a new authToken");
    }

    @Test
    @Order(2)
    public void registerUserFailure() throws Exception {
        Exception e = assertThrows(ResponseException.class, () -> {
            serverFacade.registerUser(null);
        });
        assertTrue(e.getMessage().contains("bad request") || e.getMessage().contains("unauthorized")
                || e.getMessage().contains("already taken"), "Register should properly reject bad input");
    }

    @Test
    @Order(3)
    public void logoutUserSuccess() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);
        serverFacade.logoutUser(registerResult.authToken());
        assertThrows(ResponseException.class, () -> {
            serverFacade.logoutUser(registerResult.authToken());
        }, "Should not be able to log out already logged-out user");
    }

    @Test
    @Order(4)
    public void logoutUserFailure() throws ResponseException {
        Exception e = assertThrows(ResponseException.class, () -> { serverFacade.logoutUser("unlikely");
        });
        assertTrue(e.getMessage().contains("Auth token not found"), "Should reject a nonexistent authtoken");
    }

    @Test
    @Order(5)
    public void loginUserSuccess() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);
        serverFacade.logoutUser(registerResult.authToken());

        AuthTokenData returnToken = serverFacade.loginUser(newTestUser.username(), newTestUser.password());
        assertEquals(returnToken.username(), newTestUser.username());
        assertNotNull(returnToken.authToken());
    }

    @Test
    @Order(5)
    public void loginUserFailure() throws ResponseException {
        assertThrows(ResponseException.class, () -> {serverFacade.loginUser("fakeName", "fakePW");
        }, "Should prevent nonexistent user from logging in");
    }

    @Test
    @Order(6)
    public void createGameSuccess() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        Collection<GameData> gameList = serverFacade.listGame(registerResult.authToken());
        assertTrue(gameID != 0);
    }

    @Test
    @Order(6)
    public void createGameFailure() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        assertThrows(ResponseException.class, () ->
                serverFacade.createGame(registerResult.authToken(), "testGame"));
    }

    @Test
    @Order(7)
    public void listGameSuccess_Empty() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);

        Collection<GameData> gameList = serverFacade.listGame(registerResult.authToken());

        assertTrue(gameList.isEmpty(), "Should return empty list of games");
    }

    @Test
    @Order(7)
    public void listGameSuccess_NotEmpty() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        Collection<GameData> gameList = serverFacade.listGame(registerResult.authToken());
        assertFalse(gameList.isEmpty(), "Should return populated list of games");
    }

    @Test
    @Order(7)
    public void listGameFailure() {
        assertThrows(ResponseException.class, () ->
                serverFacade.listGame("invalidToken"));
    }

    @Test
    @Order(8)
    public void joinGameSuccess_White() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        assertDoesNotThrow(() -> serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.WHITE, gameID));
    }
    @Test
    @Order(8)
    public void joinGameSuccess_Black() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        assertDoesNotThrow(() -> serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.BLACK, gameID));
    }

    @Test
    @Order(8)
    public void joinGameFailure_White() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.WHITE, gameID);

        assertThrows(ResponseException.class, () ->
                serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.WHITE, gameID));
    }

    @Test
    @Order(8)
    public void joinGameFailure_Black() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.BLACK, gameID);

        assertThrows(ResponseException.class, () ->
                serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.BLACK, gameID));
    }

    @Test
    @Order(8)
    public void joinGameFailure_NonexistentGame() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthTokenData registerResult = serverFacade.registerUser(newTestUser);

        assertThrows(ResponseException.class, () ->
                serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.WHITE, 1010101010));
    }

    @Test
    @Order(9)
    public void clearDatabaseSuccess() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        serverFacade.registerUser(newTestUser);

        serverFacade.clearDatabase();

        assertDoesNotThrow(() -> serverFacade.registerUser(newTestUser));
    }
}
