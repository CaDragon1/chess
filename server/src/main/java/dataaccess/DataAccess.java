package dataaccess;

import Models.AuthTokenData;
import Models.GameData;
import Models.UserData;
import chess.ChessGame;

import java.util.Collection;

public class DataAccess {
    /**
     * UserData methods
     */
    public UserData getUserData(String username) {

        return null;
    }
    public void addUserData(UserData userData) {

    }

    /**
     * AuthData methods
     */
    public void addAuthData (AuthTokenData authData) {

    }

    public void removeAuthData (AuthTokenData authData) {

    }

    public AuthTokenData getAuthData (AuthTokenData authData) {
        return null;
    }

    /**
     * GameData methods
     */
    public Collection<GameData> getGameList() {
        return null;
    }

    public GameData getGameByName(String gameName) {
        return null;
    }

    public GameData getGameByID(int gameID) {
        return null;
    }

    public void createGame(GameData gameData) {

    }

    public void joinGame(AuthTokenData authData, ChessGame.TeamColor team, int gameID) {

    }

    /**
     * Mass deletion methods
     */
    public void clearGames() {

    }

    public void clearUsers() {

    }

    public void clearAuthTokens() {

    }
}
