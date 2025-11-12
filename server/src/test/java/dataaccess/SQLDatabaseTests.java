package dataaccess;

import org.junit.jupiter.api.BeforeEach;

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

}
