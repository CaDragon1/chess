package passoff.server;

import dataaccess.DataAccessException;
import dataaccess.memorydao.MemoryAuthDataAccess;
import dataaccess.memorydao.MemoryUserDataAccess;
import models.AuthData;
import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.ServerException;
import service.UserService;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTests {
    private UserService service;
    private MemoryUserDataAccess userDAO;
    private MemoryAuthDataAccess authDAO;

    @BeforeEach
    public void reset() {
        userDAO = new MemoryUserDataAccess();
        authDAO = new MemoryAuthDataAccess();
        service = new UserService(userDAO, authDAO);
    }

    @Test
    public void testRegisterUserAndSucceed() throws ServerException, DataAccessException {
        UserData user = new UserData("JohnHalo", "iluvcortana", "mastercheeks@test.com");
        AuthData auth = service.register(user);

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("JohnHalo", auth.username());

        UserData testUser = userDAO.getUser("JohnHalo");
        assertEquals("iluvcortana", testUser.password());
    }

    @Test
    public void testLogoutAndSucceed() throws ServerException {
        UserData user = new UserData("JohnFortnite", "mikumiku", "orangejulius@test.com");
        AuthData auth = service.register(user);
        service.logout(auth.authToken());

        Exception e = assertThrows(DataAccessException.class, () -> {
            authDAO.getAuthData(auth.authToken());
        });
        assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    public void testLoginMultipleTimes() throws ServerException, DataAccessException {
        UserData user = new UserData("JohnFortnite", "mikumiku", "orangejulius@test.com");
        AuthData auth = service.register(user);
        service.logout(auth.authToken());
        service.login("JohnFortnite", "mikumiku");
        Exception e = assertThrows(ServerException.class, () -> {
            service.login("JohnFortnite", "mikumiku");
        });
        assertTrue(e.getMessage().contains("unauthorized"));
    }
}
