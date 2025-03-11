package dataaccess;

import Models.AuthTokenData;
import Models.GameData;
import Models.UserData;
import chess.ChessGame;

import java.util.Collection;

public interface UserDataAccess {
    /**
     * UserData methods
     */
    UserData getUserData(String username);

    void addUserData(UserData userData);

    /**
     * Mass deletion methods
     */
    void clearUsers();
}
