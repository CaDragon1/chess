package passoff.server;
import static org.junit.jupiter.api.Assertions.*;

import dataaccess.SqlAuthDataAccess;
import dataaccess.SqlGameDataAccess;
import dataaccess.SqlUserDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SQLDatabaseTests {
    private SqlUserDataAccess userDAO;
    private SqlAuthDataAccess authDAO;
    private SqlGameDataAccess gameDAO;

    @BeforeEach
    public void setup() {
        userDAO = new SqlUserDataAccess();
        authDAO = new SqlAuthDataAccess();
        gameDAO = new SqlGameDataAccess();

        userDAO.clear();
        authDAO.clear();
        gameDAO.clear();
    }
}
