package service;

import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDataAccess;
import dataaccess.interfaces.UserDataAccess;
import models.AuthData;
import models.UserData;
import server.ServerException;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {
    private final UserDataAccess userDAO;
    private final AuthDataAccess authDAO;

    public UserService(UserDataAccess userDAO, AuthDataAccess authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register (UserData userData) throws ServerException {
        // Username validity check here
        String usernameCheck = userData.username();
        if (usernameCheck == null || usernameCheck.isBlank() || userData.password() == null || userData.email() == null) {
            throw new ServerException("bad request", 400);
        }
        try {
            if (userDAO.getUser(userData.username()) != null) {
                throw new ServerException("already  taken", 403);
            }

            String hashPW = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
            UserData hashedUser = new UserData(userData.username(), hashPW, userData.email());

            userDAO.addUser(hashedUser);
            AuthData authData = new AuthData(generateToken(), hashedUser.username());
            authDAO.createAuthData(authData);
            return authData;
        } catch (DataAccessException e) {
            throw new ServerException(e.getMessage(), 500);
        }
    }

    // login should create new authData for the login session (unless I misunderstood something).
    public AuthData login(String username, String password) throws ServerException {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            throw new ServerException("bad request", 400);
        }
        try {
            UserData user = userDAO.getUser(username);

            if (user == null || !BCrypt.checkpw(password, user.password())) {
                throw new ServerException("unauthorized", 401);
            }
            // Extra check to ensure single logins
            //AuthData currentAuth = authDAO.getCurrentUserAuthToken(username);
//            if (currentAuth != null) {
//                throw new ServerException("unauthorized", 401);
//            }

            AuthData authData = new AuthData(generateToken(), username);
            authDAO.createAuthData(authData);
            return authData;
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized")) {
                throw new ServerException(e.getMessage(), 401);
            }
            throw new ServerException(e.getMessage(), 500);
        }
    }

    public void logout(String authToken) throws ServerException {
        try {
            authDAO.deleteAuthData(authToken);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized") || e.getMessage().contains("Auth token not found")) {
                throw new ServerException(e.getMessage(), 401);
            }
            throw new ServerException(e.getMessage(), 500);
        }
    }

    private String generateToken() {
//        byte[] randomBytes = new byte[24];
        return java.util.UUID.randomUUID().toString();
    }

    // Method for websocket to verify
    public AuthData getAuthDataFromToken(String authToken) throws DataAccessException {
        return authDAO.getAuthData(authToken);
    }
}
