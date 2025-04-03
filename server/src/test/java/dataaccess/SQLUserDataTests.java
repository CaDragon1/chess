package dataaccess;
import models.UserData;
import org.junit.jupiter.api.*;
import passoff.model.TestUser;
import service.Service;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLUserDataTests {
    private static final TestUser TEST_USER = new TestUser("ExistingUser", "existingUserPassword", "eu@mail.com");

    private static SqlUserDataAccess userDataAccess = new SqlUserDataAccess();

    @BeforeEach
    public void clearDatabase() throws dataaccess.ServerException {
        userDataAccess.clearUsers();
    }

    @Test
    @DisplayName("Add User Data - Success")
    @Order(1)
    public void addUserData_Success() throws Exception {
        SqlUserDataAccess userDAO = new SqlUserDataAccess();

        // Create test user
        UserData testUser = new UserData("Test_User", "testPassword!1", "testmail@email.mail");

        // Add test user to database
        userDAO.addUserData(testUser);

        // Test that it is contained in the database
        UserData fetchData = userDAO.getUserData(testUser.username());
        Assertions.assertNotNull(fetchData);
        Assertions.assertEquals(testUser.username(), fetchData.username());
        Assertions.assertEquals(testUser.password(), fetchData.password());
        Assertions.assertEquals(testUser.email(), fetchData.email());
    }

    /**
     * Add user tests
     */

    @Test
    @DisplayName("Add User Data - Null username")
    @Order(1)
    public void addUserData_NullUsername() throws Exception {
        SqlUserDataAccess userDAO = new SqlUserDataAccess();

        // Create test user
        UserData testUser = new UserData(null, "password2@", "email1@email.mail");

        // Add test user to database
        Exception e = assertThrows(dataaccess.ServerException.class, () -> {
            userDAO.addUserData(testUser);
        });

        assertTrue(e.getMessage().contains("Userdata add failed"));
    }

    @Test
    @DisplayName("Add User Data - Null password")
    @Order(1)
    public void addUserData_NullPassword() throws Exception {
        Service service = new Service();
        service.clearApp();

        // Create test user
        UserData testUser = new UserData("Testname2", null, "email2@email.mail");

        // Add test user to database
        Exception e = assertThrows(server.ServerException.class, () -> {
            service.register(testUser);
        });

        assertTrue(e.getMessage().contains("bad request"));
    }

    @Test
    @DisplayName("Add User Data - Null email")
    @Order(1)
    public void addUserData_NullEmail() throws Exception {
        Service service = new Service();
        service.clearApp();

        // Create test user
        UserData testUser = new UserData("Testname3", "password3#", null);

        // Add test user to database
        Exception e = assertThrows(server.ServerException.class, () -> {
            service.register(testUser);
        });

        assertTrue(e.getMessage().contains("bad request"));
    }

    /**
     * Get user tests. The success test is the addData test, as that test must necessarily call getUserData.
     */
    @Test
    @DisplayName("Get User Data - Failure")
    @Order(2)
    public void getUserData_Failure() throws Exception {
        SqlUserDataAccess userDAO = new SqlUserDataAccess();
        String testUsername = "nonexistentUser";

        UserData getData = userDAO.getUserData(testUsername);

        assertNull(getData);
    }

}
