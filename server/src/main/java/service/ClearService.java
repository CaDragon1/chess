package service;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDataAccess;
import dataaccess.interfaces.GameDataAccess;
import dataaccess.interfaces.UserDataAccess;
import dataaccess.memorydao.MemoryAuthDataAccess;
import dataaccess.memorydao.MemoryGameDataAccess;
import dataaccess.memorydao.MemoryUserDataAccess;

public class ClearService {
    private final MemoryUserDataAccess userDAO;
    private final MemoryGameDataAccess gameDAO;
    private final MemoryAuthDataAccess authDAO;

    public ClearService(MemoryUserDataAccess userDAO, MemoryGameDataAccess gameDAO, MemoryAuthDataAccess authDAO) {
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void clearData() throws DataAccessException {
        userDAO.clear();
        gameDAO.clear();
        authDAO.clear();
    }
}
