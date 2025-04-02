package dataaccess;
import models.UserData;
import models.AuthTokenData;
import org.junit.jupiter.api.*;
import passoff.model.TestUser;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLAuthDataTests {
    private static final TestUser TEST_USER = new TestUser("ExistingUser", "existingUserPassword", "eu@mail.com");

    private static SqlUserDataAccess userDataAccess = new SqlUserDataAccess();
    private static SqlAuthDataAccess authDataAccess = new SqlAuthDataAccess();
    private static UserData authTestUserData = new UserData("testUser", "testPassword1", "test@email.mail");


    @BeforeEach
    public void resetDatabase() throws Exception {
        try (Connection conn = DatabaseManager.getConnection();
             Statement statement = conn.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS AuthData CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS GameData CASCADE");
            statement.executeUpdate("DROP TABLE IF EXISTS UserData");
            userDataAccess.configureDatabase();
            authDataAccess.configureDatabase();
        }
    }

    @Test
    @DisplayName("Add Auth Data - Success")
    @Order(1)
    public void addAuthData_Success() throws Exception {
        userDataAccess.addUserData(authTestUserData);
        SqlAuthDataAccess authDAO = new SqlAuthDataAccess();

        // Create test authTokenData
        AuthTokenData testToken = new AuthTokenData("authToken", "Test_User");

        // Add test user to database
        authDAO.addAuthData(testToken);

        // Test that it is contained in the database
        AuthTokenData fetchData = authDAO.getAuthData(testToken.authToken());
        Assertions.assertNotNull(fetchData);
        Assertions.assertEquals(testToken.username(), fetchData.username());
        Assertions.assertEquals(testToken.authToken(), fetchData.authToken());
    }

    @Test
    @DisplayName("Add Auth Data - Null authToken")
    @Order(2)
    public void addAuthData_NullToken() throws Exception {
        SqlAuthDataAccess authDAO = new SqlAuthDataAccess();

        // Create test authTokenData
        AuthTokenData testToken = new AuthTokenData(null, "Test_User");

        // Add test user to database
        Exception e = assertThrows(dataaccess.ServerException.class, () -> {
            authDAO.addAuthData(testToken);
        });

        assertTrue(e.getMessage().contains("Authdata add failed"));
    }

    @Test
    @DisplayName("Add Auth Data - Null username")
    @Order(2)
    public void addAuthData_NullUsername() throws Exception {
        SqlAuthDataAccess authDAO = new SqlAuthDataAccess();

        // Create test authTokenData
        AuthTokenData testToken = new AuthTokenData("TestAuthToken", null);

        // Add test user to database
        Exception e = assertThrows(dataaccess.ServerException.class, () -> {
            authDAO.addAuthData(testToken);
        });

        assertTrue(e.getMessage().contains("Authdata add failed"));
    }

    @Test
    @DisplayName("Get Auth Data - Nonexistent token in database")
    @Order (3)
    public void getAuthData_NonexistentToken() throws Exception {
        SqlAuthDataAccess authDAO = new SqlAuthDataAccess();

        // Create test authTokenData
        AuthTokenData testToken = new AuthTokenData("FalseToken", "testUserDoesntExist");

        // Attempt to get authData from database
        Exception e = assertThrows(dataaccess.ServerException.class, () -> {
            authDAO.getAuthData(testToken.authToken());
        });

        assertTrue(e.getMessage().contains("Authentication token not found"));
    }

    @Test
    @DisplayName("Get Auth Data - Null token")
    @Order (3)
    public void getAuthData_NullToken() throws Exception {
        SqlAuthDataAccess authDAO = new SqlAuthDataAccess();

        // Create test authTokenData
        AuthTokenData testToken = new AuthTokenData(null, "testUserDoesntExist");

        // Attempt to get authData from database
        Exception e = assertThrows(dataaccess.ServerException.class, () -> {
            authDAO.getAuthData(testToken.authToken());
        });

        assertTrue(e.getMessage().contains("Authentication token not found"));
    }

    @Test
    @DisplayName("Remove Auth Data - Success")
    @Order (4)
    public void removeAuthData_Success() throws Exception {
        // Add a test auth token
        SqlAuthDataAccess authDAO = new SqlAuthDataAccess();
        AuthTokenData testToken = new AuthTokenData("authToken", "Test_User");
        authDAO.addAuthData(testToken);

        // Remove test token
        authDAO.removeAuthData(testToken);

        // Verify nonexistent entry
        Exception e = assertThrows(dataaccess.ServerException.class, () -> {
            authDAO.getAuthData(testToken.authToken());
        });
        assertTrue(e.getMessage().contains("Authentication token not found"));
    }

    @Test
    @DisplayName("Remove Auth Data - Token not in database")
    @Order (4)
    public void removeAuthData_TokenNonexistent() throws Exception {
        // Create a test auth token without adding it
        SqlAuthDataAccess authDAO = new SqlAuthDataAccess();
        AuthTokenData testToken = new AuthTokenData("authToken", "Test_User");

        // Attempt to remove test token
        Exception e = assertThrows(dataaccess.ServerException.class, () -> {
            authDAO.removeAuthData(testToken);
        });
        assertTrue(e.getMessage().contains("Auth token not found"));
    }
}