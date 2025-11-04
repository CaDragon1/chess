package service;

import dataaccess.DataAccessException;
import dataaccess.memorydao.MemoryAuthDataAccess;
import dataaccess.memorydao.MemoryGameDataAccess;
import dataaccess.memorydao.MemoryUserDataAccess;
import models.AuthData;
import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ServerException;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTests {
    private ClearService clearService;
    private GameService gameService;
    private UserService userService;
    private MemoryUserDataAccess userDAO;
    private MemoryGameDataAccess gameDAO;
    private MemoryAuthDataAccess authDAO;

    @BeforeEach
    public void reset() {
        userDAO = new MemoryUserDataAccess();
        gameDAO = new MemoryGameDataAccess();
        authDAO = new MemoryAuthDataAccess();
        clearService = new ClearService(userDAO, gameDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO, userDAO);
        userService = new UserService(userDAO, authDAO);
    }

    // Success: Can clear data
    @Test
    public void testClearSuccess() throws ServerException, DataAccessException {
        AuthData auth = userService.register(new UserData("mr boombastic", "fantastic", "mrromantic@email.com"));
        gameService.createGame(auth.authToken(), "shaggy");

        clearService.clearData();

        assertNull(userDAO.getUser("mr boombastic"));
        assertEquals(0, gameDAO.listGames().size());
        DataAccessException e = assertThrows(DataAccessException.class, () -> assertNull(authDAO.getAuthData("1984152347")));
        assertTrue(e.getMessage().contains("unauthorized"));

    }
}