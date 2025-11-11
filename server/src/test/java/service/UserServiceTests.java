package service;

import dataaccess.DataAccessException;
import dataaccess.memorydao.MemoryAuthDataAccess;
import dataaccess.memorydao.MemoryUserDataAccess;
import models.AuthData;
import models.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import server.ServerException;

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
        assertTrue(BCrypt.checkpw("iluvcortana", testUser.password()));
    }

    @Test
    public void testBadRegisterUserAndFail(){
        UserData user = new UserData("", "iluvcortana", "mastercheeks@test.com");
        Exception e = assertThrows(ServerException.class, () -> service.register(user));

        assertTrue(e.getMessage().contains("bad request"));
    }

    @Test
    public void testLogoutAndSucceed() throws ServerException {
        UserData user = new UserData("JohnFortnite", "mikumiku", "orangejulius@test.com");
        AuthData auth = service.register(user);
        service.logout(auth.authToken());

        Exception e = assertThrows(DataAccessException.class, () -> authDAO.getAuthData(auth.authToken()));
        assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    public void testLogoutAndFail(){
        Exception e = assertThrows(ServerException.class, () -> service.logout(null));
        assertTrue(e.getMessage().contains("unauthorized"));
    }

    @Test
    public void testLoginAndSucceed() throws ServerException{
        UserData user = new UserData("JohnFortnite", "mikumiku", "orangejulius@test.com");
        AuthData auth1 = service.register(user);
        service.logout(auth1.authToken());
        AuthData auth2 = service.login("JohnFortnite", "mikumiku");
        assertNotNull(auth2);
    }

    @Test
    public void testBadLoginAndFail(){
        Exception e = assertThrows(ServerException.class, () -> service.login("userNotFound", "sdiybt"));

        assertTrue(e.getMessage().contains("unauthorized"));
    }
}
