package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDataAccess;
import dataaccess.interfaces.GameDataAccess;
import models.AuthData;
import models.GameData;
import org.jetbrains.annotations.NotNull;
import server.ServerException;

import java.util.Collection;
import java.util.Random;

public class GameService {
    private final GameDataAccess gameDAO;
    private final AuthDataAccess authDAO;

    public GameService(GameDataAccess gameDAO, AuthDataAccess authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public int createGame(String authToken, String gameName) throws ServerException {
        try {
            AuthData authData = authDAO.getAuthData(authToken);
            if (authData != null) {
                int gameID = generateGameID();
                // verify that there's no duplicate game id
                while (gameDAO.getGame(gameID) != null) {
                    gameID = generateGameID();
                }
                GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
                gameDAO.createGame(game);
                return gameID;
            }
            throw new ServerException("unauthorized", 401);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized") || e.getMessage().contains("AuthToken not found")) {
                throw new ServerException("unauthorized", 401);
            }
            throw new ServerException(e.getMessage(), 500);
        }
    }

    public void joinGame(String authData, String team, int gameID) throws ServerException {
        try {
            // nifty conversion trick I found online
            AuthData auth = authDAO.getAuthData(authData);
            GameData game = gameDAO.getGame(gameID);
            if (auth == null) {
                throw new ServerException("unauthorized", 401);
            }
            if (game == null) {
                throw new ServerException("bad request", 400);
            }

            if (team == null) {
                throw new ServerException("bad request: null team color", 400);
            }
            game = getGameDataFromTeam(team, game, auth);
            gameDAO.updateGame(game);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized") || e.getMessage().contains("AuthToken not found")) {
                throw new ServerException("unauthorized", 401);
            }
            throw new ServerException(e.getMessage(), 500);
        }
    }

    @NotNull
    private static GameData getGameDataFromTeam(String team, GameData game, AuthData auth) throws ServerException {

        if (team.equalsIgnoreCase("BLACK")) {
            if (game.blackUsername() != null) {
                throw new ServerException("Player already exists: Black playerColor", 403);
            }
            game = game.setBlackUsername(auth.username());
        } else if (team.equalsIgnoreCase("WHITE")){
            if (game.whiteUsername() != null) {
                throw new ServerException("Player already exists: White playerColor", 403);
            }
            game = game.setWhiteUsername(auth.username());
        } else {
            throw new ServerException("bad request", 400);
        }
        return game;
    }

    public Collection<GameData> listGames(String authToken) throws ServerException {
        try {
            AuthData authData = authDAO.getAuthData(authToken);
            if (authData != null) {
                return gameDAO.listGames();
            }
            throw new ServerException("unauthorized", 401);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("unauthorized") || e.getMessage().contains("AuthToken not found")) {
                throw new ServerException("unauthorized", 401);
            }
            throw new ServerException(e.getMessage(), 500);
        }
    }

    private int generateGameID() {
        Random rand = new Random();
        int id = rand.nextInt();
        if (id < 0) {
            id*=-1;
        }
        return id;
    }

}
