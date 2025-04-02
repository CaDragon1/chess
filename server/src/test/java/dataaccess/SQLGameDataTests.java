package dataaccess;
import chess.ChessGame;
import models.UserData;
import models.AuthTokenData;
import models.GameData;
import org.junit.jupiter.api.*;
import passoff.model.TestUser;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLGameDataTests {
    private static final ChessGame testGame = new ChessGame();
    private static final GameData testGameData = new GameData(1, null, null,
            "testGame", testGame);
    private static final AuthTokenData testAuthToken = new AuthTokenData("amazingToken", "testUser");
    private static final UserData testUser = new UserData("testUser", "testPassword", "test@email.mail");

    private static final SqlUserDataAccess userDataAccess = new SqlUserDataAccess();
    private static final SqlGameDataAccess gameDataAccess = new SqlGameDataAccess();
    private static final SqlAuthDataAccess authDataAccess = new SqlAuthDataAccess();

    @BeforeEach
    public void resetDatabase() throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS AuthData CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS GameData CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS UserData");

            userDataAccess.configureDatabase();
            gameDataAccess.configureDatabase();
            authDataAccess.configureDatabase();
        }
    }
    @Test
    @DisplayName("Create Game - Success")
    @Order(1)
    public void createGame_Success() throws Exception {
        // Add game to gamedataaccess
        gameDataAccess.createGame(testGameData);

        // Verify that the game exists
        GameData fetchGame = gameDataAccess.getGameByID(testGameData.gameID());

        Assertions.assertNotNull(fetchGame);
        assertEquals(fetchGame.gameName(), testGameData.gameName());
        assertEquals(fetchGame.gameID(), testGameData.gameID());
        assertEquals(fetchGame.game(), testGameData.game());
    }

    @Test
    @DisplayName("Get Game By Name - Success")
    @Order(2)
    public void getGameByName_Success() throws Exception {
        gameDataAccess.createGame(testGameData);

        GameData fetchGame = gameDataAccess.getGameByName(testGameData.gameName());

        Assertions.assertNotNull(fetchGame);
        assertEquals(fetchGame.gameName(), testGameData.gameName());
        assertEquals(fetchGame.gameID(), testGameData.gameID());
        assertEquals(fetchGame.game(), testGameData.game());
    }

    @Test
    @DisplayName("Get Game By Name - Failure")
    @Order(2)
    public void getGameByName_Failure() throws Exception {
        Exception e = assertThrows(dataaccess.ServerException.class, () -> {
            GameData fetchGame = gameDataAccess.getGameByName(testGameData.gameName());
        });
        assertTrue(e.getMessage().contains("Invalid game name"));
    }

    @Test
    @DisplayName("Get Game By ID - Failure")
    @Order(2)
    public void getGameByID_Failure() throws Exception {
        Exception e = assertThrows(dataaccess.ServerException.class, () -> {
            GameData fetchGame = gameDataAccess.getGameByID(testGameData.gameID());
        });
        assertTrue(e.getMessage().contains("Invalid game ID"));
    }

    @Test
    @DisplayName("Join Game (White team) - Success")
    @Order (3)
    public void joinGameWhite_Success() throws Exception {
        // Add game to gamedataaccess
        gameDataAccess.createGame(testGameData);
        userDataAccess.addUserData(testUser);
        authDataAccess.addAuthData(testAuthToken);

        gameDataAccess.joinGame(testAuthToken, ChessGame.TeamColor.WHITE, testGameData.gameID());

        GameData fetchGame = gameDataAccess.getGameByID(testGameData.gameID());

        assertNotNull(fetchGame);
        assertEquals(fetchGame.gameName(), testGameData.gameName());
        assertEquals(fetchGame.gameID(), testGameData.gameID());
        assertEquals(fetchGame.game(), testGameData.game());
        assertEquals(fetchGame.whiteUsername(), testUser.username());
    }

    @Test
    @DisplayName("Join Game (Black team) - Success")
    @Order (3)
    public void joinGameBlack_Success() throws Exception {
        // Add game to gamedataaccess
        gameDataAccess.createGame(testGameData);
        userDataAccess.addUserData(testUser);
        authDataAccess.addAuthData(testAuthToken);

        gameDataAccess.joinGame(testAuthToken, ChessGame.TeamColor.BLACK, testGameData.gameID());

        GameData fetchGame = gameDataAccess.getGameByID(testGameData.gameID());

        assertNotNull(fetchGame);
        assertEquals(fetchGame.gameName(), testGameData.gameName());
        assertEquals(fetchGame.gameID(), testGameData.gameID());
        assertEquals(fetchGame.game(), testGameData.game());
        assertEquals(fetchGame.blackUsername(), testUser.username());
    }

    @Test
    @DisplayName("Join Game (White team) - Preexisting user")
    @Order (3)
    public void joinGameWhite_PreexistingUser() throws Exception {
        // Add game to gamedataaccess
        gameDataAccess.createGame(testGameData);
        userDataAccess.addUserData(testUser);
        authDataAccess.addAuthData(testAuthToken);

        // Create additional user
        UserData tempUser = new UserData("AnotherUser2", "myPassword", "itsMail@mail.ail");
        AuthTokenData tempToken = new AuthTokenData("theTokenOfChampions", tempUser.username());
        userDataAccess.addUserData(tempUser);
        authDataAccess.addAuthData(tempToken);

        gameDataAccess.joinGame(testAuthToken, ChessGame.TeamColor.WHITE, testGameData.gameID());

        Exception e = assertThrows(ServerException.class, () -> {
        gameDataAccess.joinGame(tempToken, ChessGame.TeamColor.WHITE, testGameData.gameID());
        });
        assertTrue(e.getMessage().contains("Join game failed"));
    }

    @Test
    @DisplayName("Join Game (Black team) - Preexisting user")
    @Order (3)
    public void joinGameBlack_PreexistingUser() throws Exception {
        // Add game to gamedataaccess
        gameDataAccess.createGame(testGameData);
        userDataAccess.addUserData(testUser);
        authDataAccess.addAuthData(testAuthToken);

        // Create additional user
        UserData tempUser = new UserData("AnotherUser2", "myPassword", "itsMail@mail.ail");
        AuthTokenData tempToken = new AuthTokenData("theTokenOfChampions", tempUser.username());
        userDataAccess.addUserData(tempUser);
        authDataAccess.addAuthData(tempToken);

        gameDataAccess.joinGame(testAuthToken, ChessGame.TeamColor.BLACK, testGameData.gameID());

        Exception e = assertThrows(ServerException.class, () -> {
            gameDataAccess.joinGame(tempToken, ChessGame.TeamColor.BLACK, testGameData.gameID());
        });
        assertTrue(e.getMessage().contains("Join game failed"));
    }

    @Test
    @DisplayName("Join Game (White team) - Invalid Game")
    @Order (3)
    public void joinGameWhite_InvalidGame() throws Exception {
        userDataAccess.addUserData(testUser);
        authDataAccess.addAuthData(testAuthToken);

        Exception e = assertThrows(ServerException.class, () -> {
            gameDataAccess.joinGame(testAuthToken, ChessGame.TeamColor.WHITE, testGameData.gameID());
        });
        assertTrue(e.getMessage().contains("Join game failed"));
    }

    @Test
    @DisplayName("Join Game (Black team) - Invalid Game")
    @Order (3)
    public void joinGameBlack_InvalidGame() throws Exception {
        userDataAccess.addUserData(testUser);
        authDataAccess.addAuthData(testAuthToken);

        Exception e = assertThrows(ServerException.class, () -> {
            gameDataAccess.joinGame(testAuthToken, ChessGame.TeamColor.BLACK, testGameData.gameID());
        });
        assertTrue(e.getMessage().contains("Join game failed"));
    }

    @Test
    @DisplayName("Join Game (White team) - Invalid Token")
    @Order (3)
    public void joinGameWhite_InvalidToken() throws Exception {
        gameDataAccess.createGame(testGameData);

        Exception e = assertThrows(ServerException.class, () -> {
            gameDataAccess.joinGame(testAuthToken, ChessGame.TeamColor.WHITE, testGameData.gameID());
        });
        assertTrue(e.getMessage().contains("Join game failed"));
    }

    @Test
    @DisplayName("Join Game (Black team) - Invalid Token")
    @Order (3)
    public void joinGameBlack_InvalidToken() throws Exception {
        gameDataAccess.createGame(testGameData);

        Exception e = assertThrows(ServerException.class, () -> {
            gameDataAccess.joinGame(testAuthToken, ChessGame.TeamColor.BLACK, testGameData.gameID());
        });
        assertTrue(e.getMessage().contains("Join game failed"));
    }

    @Test
    @DisplayName("Clear Games - Success")
    @Order (4)
    public void clearGames_Success() throws Exception {
        gameDataAccess.createGame(testGameData);

        gameDataAccess.clearGames();

        Exception e = assertThrows(ServerException.class, () -> {
            gameDataAccess.getGameByID(testGameData.gameID());
        });
        assertTrue(e.getMessage().contains("Invalid game ID"));
    }
}
