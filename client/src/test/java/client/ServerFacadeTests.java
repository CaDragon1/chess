package client;

import chess.ChessGame;
import exception.ResponseException;
import models.AuthData;
import models.GameData;
import models.UserData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private ServerFacade serverFacade;
    //private int port = server.run(8080);

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
    }

    @BeforeEach
    public void serverInit() throws ResponseException {
        serverFacade = new ServerFacade("http://localhost:8080");
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
        AuthData registerResult = serverFacade.registerUser(newTestUser);
        Assertions.assertNotNull(registerResult, "Should return a new authToken");
    }

    @Test
    @Order(2)
    public void registerUserFailure() throws Exception {
        ResponseException e = assertThrows(ResponseException.class, () ->
                serverFacade.registerUser(null)
        );
        assertTrue(e.getMessage().contains("bad request"));
    }

    @Test
    @Order(3)
    public void logoutUserSuccess() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthData registerResult = serverFacade.registerUser(newTestUser);
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
        assertTrue(e.getMessage().contains("unauthorized"), "Should reject a nonexistent authtoken");
    }

    @Test
    @Order(5)
    public void loginUserSuccess() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthData registerResult = serverFacade.registerUser(newTestUser);
        serverFacade.logoutUser(registerResult.authToken());

        AuthData returnToken = serverFacade.loginUser(newTestUser.username(), newTestUser.password());
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
        AuthData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        Collection<GameData> gameList = serverFacade.listGame(registerResult.authToken());
        assertTrue(gameID != 0);
    }

    @Test
    @Order(6)
    public void createGameFailure() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthData registerResult = serverFacade.registerUser(newTestUser);
        assertThrows(ResponseException.class, () ->
                serverFacade.createGame(null, "testGame"));
    }

    @Test
    @Order(7)
    public void listGameSuccess_Empty() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthData registerResult = serverFacade.registerUser(newTestUser);

        Collection<GameData> gameList = serverFacade.listGame(registerResult.authToken());

        assertTrue(gameList.isEmpty(), "Should return empty list of games");
    }

    @Test
    @Order(7)
    public void listGameSuccess_NotEmpty() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthData registerResult = serverFacade.registerUser(newTestUser);
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
        AuthData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        assertDoesNotThrow(() -> serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.WHITE, gameID));
    }
    @Test
    @Order(8)
    public void joinGameSuccess_Black() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        assertDoesNotThrow(() -> serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.BLACK, gameID));
    }

    @Test
    @Order(8)
    public void joinGameFailure_White() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.WHITE, gameID);

        assertThrows(ResponseException.class, () ->
                serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.WHITE, gameID));
    }

    @Test
    @Order(8)
    public void joinGameFailure_Black() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthData registerResult = serverFacade.registerUser(newTestUser);
        int gameID = serverFacade.createGame(registerResult.authToken(), "testGame");

        serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.BLACK, gameID);

        assertThrows(ResponseException.class, () ->
                serverFacade.joinGame(registerResult.authToken(), ChessGame.TeamColor.BLACK, gameID));
    }

    @Test
    @Order(8)
    public void joinGameFailure_NonexistentGame() throws ResponseException {
        UserData newTestUser = new UserData("testUser", "testPW", "test@game.chess");
        AuthData registerResult = serverFacade.registerUser(newTestUser);

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
