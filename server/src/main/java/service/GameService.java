package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.interfaces.UserDataAccess;
import dataaccess.memorydao.MemoryAuthDataAccess;
import dataaccess.memorydao.MemoryGameDataAccess;
import models.AuthData;
import models.GameData;
import server.ServerException;

import java.util.Collection;
import java.util.Random;

public class GameService {
    private final MemoryGameDataAccess gameDAO;
    private final MemoryAuthDataAccess authDAO;

    public GameService(MemoryGameDataAccess gameDAO, MemoryAuthDataAccess authDAO, UserDataAccess userDAO) {
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
            throw new ServerException("Unauthorized", 401);
        } catch (DataAccessException e) {
            throw new ServerException(e.getMessage(), 500);
        }
    }

    public void joinGame(String authData, ChessGame.TeamColor joinTeam, int gameID) throws ServerException {
        try {
            System.out.println("Attempting join game...\nauthData - " + authData + "\nplayerColor color - " + joinTeam.toString() + "\ngame ID - " + gameID);
            AuthData auth = authDAO.getAuthData(authData);
            GameData game = gameDAO.getGame(gameID);
            if (auth == null) {
                throw new ServerException("unauthorized", 401);
            }
            if (game == null) {
                throw new ServerException("bad request", 400);
            }

            if (joinTeam == ChessGame.TeamColor.BLACK) {
                if (game.blackUsername() != null) {
                    throw new ServerException("Player already exists: Black playerColor", 403);
                }
                game.setBlackUsername(auth.username());
            } else {
                if (game.whiteUsername() != null) {
                    throw new ServerException("Player already exists: White playerColor", 403);
                }
                game.setWhiteUsername(auth.username());
            }
            gameDAO.updateGame(game);
        } catch (DataAccessException e) {
            throw new ServerException(e.getMessage(), 500);
        }
    }

    public Collection<GameData> listGames(String authToken) throws ServerException {
        try {
            AuthData authData = authDAO.getAuthData(authToken);
            if (authData != null) {
                return gameDAO.listGames();
            }
            throw new ServerException("unauthorized", 401);
        } catch (DataAccessException e) {
            throw new ServerException(e.getMessage(), 500);
        }
    }

    private int generateGameID() {
        Random rand = new Random();
        return rand.nextInt();
    }

}
