package service;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDataAccess;
import dataaccess.interfaces.UserDataAccess;
import kotlin.NotImplementedError;
import models.AuthData;
import models.UserData;

public class UserService {
    private final UserDataAccess userDAO;
    private final AuthDataAccess authDAO;

    public UserService(UserDataAccess userDAO, AuthDataAccess authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register (UserData userData) throws DataAccessException {
        // Username validity check should probably go here
        String usernameCheck = userData.username();
        if (usernameCheck == null || usernameCheck.replaceAll("\\s", "").isEmpty()) {
            throw new DataAccessException("Empty username not allowed");
        }
        else if (userDAO.getUser(userData.username()) != null) {
            throw new DataAccessException("Username taken");
        }
        userDAO.addUser(userData);
        AuthData authData = new AuthData(generateToken(), userData.username());
        authDAO.createAuthData(authData);
        return authData;
    }

    // login should create new authData for the login session (unless I misunderstood something).
    public AuthData login(String username, String password) throws DataAccessException {
        if(userDAO.getUser(username) == null || userDAO.getUser(username).password().equals(password)) {
            throw new DataAccessException("Invalid username or password");
        }
        AuthData authData = new AuthData(generateToken(), username);
        authDAO.createAuthData(authData);
        return authData;
    }

    public void logout(String authToken) throws DataAccessException {
        authDAO.deleteAuthData(authToken);
    }

    private String generateToken() {
//        byte[] randomBytes = new byte[24];
        return java.util.UUID.randomUUID().toString();
    }
}
