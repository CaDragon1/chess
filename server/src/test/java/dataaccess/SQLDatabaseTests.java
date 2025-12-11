package dataaccess;

import chess.ChessGame;
import models.AuthData;
import models.GameData;
import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

import static org.junit.jupiter.api.Assertions.*;

public class SQLDatabaseTests {
    private SqlUserDataAccess userDAO;
    private SqlAuthDataAccess authDAO;
    private SqlGameDataAccess gameDAO;

    @BeforeEach
    public void setup() throws DataAccessException {
        userDAO = new SqlUserDataAccess();
        authDAO = new SqlAuthDataAccess();
        gameDAO = new SqlGameDataAccess();

        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }

    /**
     * UserDAO tests
     */

    @Test @DisplayName("User add success")
    public void testUserDAOAddSuccess() throws DataAccessException {
        userDAO.addUser(new UserData("bobby_tables", "xkcd", "mail@mail.com"));
        UserData result = userDAO.getUser("bobby_tables");
        assertEquals("bobby_tables", result.username());
    }

    @Test @DisplayName("User add duplicate failure")
    public void testUserDAOAddDupeFailure() throws DataAccessException {
        userDAO.addUser(new UserData("bobby_tables", "xkcd", "mail@mail.com"));

        // expect thrown exception on duplicate add
        assertThrows(DataAccessException.class, () -> {
            userDAO.addUser(new UserData("bobby_tables", "xkcd2", "mail2@mail.com"));
        });
    }

    @Test @DisplayName("User get success") // this one's redundant, but oh well. It's already written at this point
    public void testUserGetSuccess() throws DataAccessException {
        userDAO.addUser(new UserData("bobby_hill", "hwat", "mail@mail.com"));
        UserData result = userDAO.getUser("bobby_hill");
        assertEquals("bobby_hill", result.username());
        assertEquals("mail@mail.com", result.email());
    }

    @Test @DisplayName("User get failure - non-existent username")
    public void testUserGetFailure() throws DataAccessException {
        assertNull(userDAO.getUser("nonexistent_user"));
    }

    @Test @DisplayName("User clear success")
    public void testUserDAOClearSuccess() throws DataAccessException {
        userDAO.addUser(new UserData("bobby_tables", "xkcd", "mail@mail.com"));
        userDAO.clear();
        assertNull(userDAO.getUser("bobby_tables"));
    }

    /**
     * AuthDAO tests
     */

    @Test @DisplayName("Auth create success")
    public void testCreateAuthDataSuccess() throws DataAccessException {
        authDAO.createAuthData(new AuthData("01134", "riku"));
        AuthData result = authDAO.getAuthData("01134");
        assertEquals("01134", result.authToken());
        assertEquals("riku", result.username());
    }

    @Test @DisplayName("Auth create duplicate failure")
    public void testCreateAuthDataDupeFailure() throws DataAccessException {
        authDAO.createAuthData(new AuthData("01134", "riku"));

        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuthData(new AuthData("01134", "kairi"));
        });
    }

    @Test @DisplayName("Auth get success")
    public void testAuthGetSuccess() throws DataAccessException {
        authDAO.createAuthData(new AuthData("token123", "sora"));
        AuthData result = authDAO.getAuthData("token123");
        assertEquals("sora", result.username());
    }

    @Test @DisplayName("Auth get failure")
    public void testAuthGetFailure() {
        assertThrows(DataAccessException.class, () -> {
            authDAO.getAuthData("invalid_token");
        });
    }

    @Test @DisplayName("Auth delete success")
    public void testAuthDeleteSuccess() throws DataAccessException {
        authDAO.createAuthData(new AuthData("80085", "sora"));
        authDAO.deleteAuthData("80085");
        assertThrows(DataAccessException.class, () -> {
            authDAO.getAuthData("80085");
        });
    }

    @Test @DisplayName("Auth delete failure")
    public void testAuthDeleteFailure() {
        assertThrows(DataAccessException.class, () -> {
            authDAO.deleteAuthData("nonexistent_token");
        });
    }

    @Test @DisplayName("Auth clear success")
    public void testAuthClearSuccess() throws DataAccessException {
        authDAO.createAuthData(new AuthData("angus45", "beefcake"));
        authDAO.clear();
        assertThrows(DataAccessException.class, () -> {
            authDAO.getAuthData("angus45");
        });
    }

    // ===== GAME DAO TESTS =====
    @Test @DisplayName("Game list empty")
    public void testGameListEmpty() throws DataAccessException {
        assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test @DisplayName("Game list success")
    public void testGameListSuccess() throws DataAccessException {
        gameDAO.createGame(new GameData(1, null, null,
                "floppa", new ChessGame(), GameData.GameStatus.PREGAME));
        assertFalse(gameDAO.listGames().isEmpty());
    }

    @Test @DisplayName("Game get success")
    public void testGameGetSuccess() throws DataAccessException {
        userDAO.addUser(new UserData("peepo", "lilguy", "weehyper@kek.net"));
        GameData game = new GameData(420, "peepo", null,
                "floppa", new ChessGame(), GameData.GameStatus.PREGAME);
        gameDAO.createGame(game);
        assertEquals(game.gameName(), gameDAO.getGame(420).gameName());
    }

    @Test @DisplayName("Game get failure")
    public void testGameGetFailure() throws DataAccessException {
        assertNull(gameDAO.getGame(69));
    }

    @Test @DisplayName("Game create success")
    public void testGameCreateSuccess() throws DataAccessException {
        GameData game = new GameData(21, null, null,
                "bingus", new ChessGame(), GameData.GameStatus.PREGAME);
        gameDAO.createGame(game);
        assertNotNull(gameDAO.getGame(21));
    }

    @Test @DisplayName("Game create duplicate failure")
    public void testGameCreateDuplicateFailure() throws DataAccessException {
        GameData game = new GameData(42, null, null,
                "pepe", new ChessGame(), GameData.GameStatus.PREGAME);
        gameDAO.createGame(game);
        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(game);
        });
    }

    @Test @DisplayName("Game update success")
    public void testGameUpdateSuccess() throws DataAccessException {
        userDAO.addUser(new UserData("markiplier", "helloeverybody", "mark@email.com"));

        GameData original = new GameData(67, null, null,
                "freddy", new ChessGame(), GameData.GameStatus.PREGAME);
        GameData updated = new GameData(67, "markiplier", null,
                "fastbear", new ChessGame(), GameData.GameStatus.LIVE);

        gameDAO.createGame(original);
        gameDAO.updateGame(updated);
        assertEquals("fastbear", gameDAO.getGame(67).gameName());
        assertEquals("markiplier", gameDAO.getGame(67).whiteUsername());
    }

    @Test @DisplayName("Game update failure")
    public void testGameUpdateFailure() throws DataAccessException {
        userDAO.addUser(new UserData("Gonzo", "TheGreat", "lovechickens@gonzomail.com"));

        GameData game = new GameData(999, "Gonzo", null,
                "muppetshow", new ChessGame(), GameData.GameStatus.LIVE);
        assertNull(gameDAO.getGame(999));
    }

    @Test @DisplayName("Game clear success")
    public void testGameClearSuccess() throws DataAccessException {
        gameDAO.createGame(new GameData(1, null, null,
                "kermit", new ChessGame(), GameData.GameStatus.PREGAME));
        gameDAO.clear();
        assertTrue(gameDAO.listGames().isEmpty());
    }
}
