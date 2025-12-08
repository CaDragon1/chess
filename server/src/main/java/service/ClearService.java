package service;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDataAccess;
import dataaccess.interfaces.GameDataAccess;
import dataaccess.interfaces.UserDataAccess;

public class ClearService {
    private final UserDataAccess userDAO;
    private final GameDataAccess gameDAO;
    private final AuthDataAccess authDAO;

    /**
     * Constructor for clearservice
     * @param userDAO is the user dao with all userdata stored
     * @param gameDAO is the game dao with all games stored
     * @param authDAO is, you guessed it, the same thing but for authdata
     */
    public ClearService(UserDataAccess userDAO, GameDataAccess gameDAO, AuthDataAccess authDAO) {
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    /**
     * Method to clear all the DAO objects
     * @throws DataAccessException if the clears have an issue
     */
    public void clearData() throws DataAccessException {
        userDAO.clear();
        gameDAO.clear();
        authDAO.clear();
    }
}
