package service;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDataAccess;
import dataaccess.interfaces.GameDataAccess;
import dataaccess.interfaces.UserDataAccess;

public class ClearService {
    private final UserDataAccess userDAO;
    private final GameDataAccess gameDAO;
    private final AuthDataAccess authDAO;

    public ClearService(UserDataAccess userDAO, GameDataAccess gameDAO, AuthDataAccess authDAO) {
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
