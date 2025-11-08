package service;

import dataaccess.DataAccessException;
import dataaccess.memorydao.MemoryGameDataAccess;
import dataaccess.memorydao.MemoryAuthDataAccess;
import dataaccess.memorydao.MemoryUserDataAccess;
import models.AuthData;
import models.GameData;
import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ServerException;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTests {
    private GameService gameService;
    private UserService userService;
    private MemoryGameDataAccess gameDAO;

    @BeforeEach
    public void reset() {
        gameDAO = new MemoryGameDataAccess();
        MemoryAuthDataAccess authDAO = new MemoryAuthDataAccess();
        MemoryUserDataAccess userDAO = new MemoryUserDataAccess();
        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
    }

    // Create Game
    @Test
    public void testCreateGameSuccess() throws ServerException, DataAccessException {
        UserData user = new UserData("newPlayer", "password1", "insanemail@email.com");

        AuthData authData = userService.register(user);

        int gameID = gameService.createGame(authData.authToken(), "test");

        GameData game = gameDAO.getGame(gameID);
        assertNotNull(game);
    }

    @Test
    public void testCreateGameFailure() {
        Exception e = assertThrows(ServerException.class, () -> gameService.createGame(null, ""));
        assertTrue(e.getMessage().contains("unauthorized") || e.getMessage().contains("bad request"));
    }

    // List Game
    @Test
    public void testListGamesSuccess() throws ServerException {
        UserData user = new UserData("memelord", "beegyoshi", "anotheremail@example.com");

        AuthData auth = userService.register(user);
        gameService.createGame(auth.authToken(), "gamer");

        Collection<GameData> games = gameService.listGames(auth.authToken());

        assertNotNull(games);
        assertFalse(games.isEmpty());
    }

    @Test
    public void testListGamesUnauthorized() throws ServerException {
        UserData user = new UserData("memelord", "beegyoshi", "anotheremail@example.com");
        AuthData auth = userService.register(user);
        userService.logout(auth.authToken());
        Exception e = assertThrows(ServerException.class, () -> gameService.listGames(auth.authToken()));
        assertTrue(e.getMessage().contains("unauthorized"));
    }

    // Join Game
    @Test
    public void testJoinGameSuccess() throws ServerException, DataAccessException {
        UserData user = new UserData("memelord", "beegyoshi", "anotheremail@example.com");

        AuthData auth = userService.register(user);
        int gameID = gameService.createGame(auth.authToken(), "gamer");

        gameService.joinGame(auth.authToken(), "WHITE", gameID);

        assertEquals("memelord", gameDAO.getGame(gameID).whiteUsername());
    }

    @Test
    public void testJoinGameFailure() throws ServerException {
        UserData user = new UserData("memelord", "beegyoshi", "anotheremail@example.com");

        AuthData auth = userService.register(user);
        int gameID = gameService.createGame(auth.authToken(), "gamer");

        Exception e = assertThrows(ServerException.class, () -> gameService.joinGame(auth.authToken(), null, gameID));
        assertTrue(e.getMessage().contains("null team color"));
    }
}
