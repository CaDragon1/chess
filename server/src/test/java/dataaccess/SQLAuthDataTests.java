package dataaccess;
import models.UserData;
import models.AuthTokenData;
import org.junit.jupiter.api.*;
import passoff.model.TestUser;
import service.Service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLAuthDataTests {
    private static final TestUser TEST_USER = new TestUser("ExistingUser", "existingUserPassword", "eu@mail.com");

    private static SqlAuthDataAccess authDataAccess = new SqlAuthDataAccess();

    @BeforeEach
    public void clearDatabase() throws dataaccess.ServerException {
        authDataAccess.clearAuthTokens();
    }

    @Test
    @DisplayName("Add Auth Data - Success")
    @Order(1)
    public void addAuthData_Success() throws Exception {
        SqlAuthDataAccess authDAO = new SqlAuthDataAccess();

        // Create test authTokenData
        AuthTokenData testToken = new AuthTokenData("authToken","Test_User");

        // Add test user to database
        authDAO.addAuthData(testToken);

        // Test that it is contained in the database
        AuthTokenData fetchData = authDAO.getAuthData(testToken.authToken());
        Assertions.assertNotNull(fetchData);
        Assertions.assertEquals(testToken.username(), fetchData.username());
        Assertions.assertEquals(testToken.authToken(), fetchData.authToken());
    }