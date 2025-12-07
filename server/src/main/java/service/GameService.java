package service;

import chess.ChessGame;
import chess.InvalidMoveException;
import dataaccess.DataAccessException;
import dataaccess.interfaces.AuthDataAccess;
import dataaccess.interfaces.GameDataAccess;
import models.AuthData;
import models.GameData;
import org.jetbrains.annotations.NotNull;
import server.ServerException;

import java.util.List;
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

    public List<GameData> listGames(String authToken) throws ServerException {
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

    // Functions to get games and make moves via the websocket
    public GameData getGame(int gameID) throws ServerException {
        try {
            return gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new ServerException("Game not found: " + e.getMessage(), 404);
        }
    }

    public GameData makeMove(String authToken, int gameID, chess.ChessMove move) throws ServerException {
        try {
            AuthData auth = authDAO.getAuthData(authToken);
            if (auth == null) {
                throw new ServerException("unauthorized", 401);
            }
            GameData gameData = gameDAO.getGame(gameID);
            if (gameData == null) {
                throw new ServerException("bad request", 400);
            }
            if (getGameStatus(gameData.game(), gameData) == GameData.GameStatus.PREGAME) {
                throw new ServerException("game requires two players to begin", 403);
            }
            ChessGame game = gameData.game();
            ChessGame.TeamColor currentTeam = game.getTeamTurn();

            if (verifyMovementPossible(gameData, auth, currentTeam, game)) {
                game.makeMove(move);
            }
            GameData.GameStatus status = getGameStatus(game, gameData);

            GameData updatedGame = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game, getGameStatus(game, gameData));
            gameDAO.updateGame(updatedGame);
            return updatedGame;
        } catch (DataAccessException e) {
            throw new ServerException("bad request", 400);
        } catch (InvalidMoveException e) {
            throw new ServerException("illegal move", 403);
        }
    }

    // Function to obtain the status of a game and determine changes to that status
    private static GameData.GameStatus getGameStatus(ChessGame game, GameData currentData) {
        if (game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK)) {
            return GameData.GameStatus.STALEMATE;
        }
        ChessGame.TeamColor currentTurn = game.getTeamTurn();
        if (game.isInCheckmate(currentTurn)) {
            return currentTurn == ChessGame.TeamColor.WHITE ?
                    GameData.GameStatus.BLACK_WIN : GameData.GameStatus.WHITE_WIN;
        }
        if (currentData.blackUsername() == null || currentData.whiteUsername() == null) {
            return GameData.GameStatus.PREGAME;
        }
        if (currentData.status() == GameData.GameStatus.RESIGNED) {
            return GameData.GameStatus.RESIGNED;
        }
        return GameData.GameStatus.LIVE;
    }

    // Helper function to verify that it is possible to move (potentially superfluous)
    private static boolean verifyMovementPossible(GameData gameData, AuthData auth, ChessGame.TeamColor currentTeam, ChessGame game) throws ServerException {
        // Ensure it is the correct turn for the move to be made
        boolean whitePlayer = gameData.whiteUsername() != null && gameData.whiteUsername().equalsIgnoreCase(auth.username());
        boolean blackPlayer = gameData.blackUsername() != null && gameData.blackUsername().equalsIgnoreCase(auth.username());
        if (currentTeam == ChessGame.TeamColor.WHITE && !whitePlayer) {
            throw new ServerException("Not white team's turn", 403);
        }
        else if (currentTeam == ChessGame.TeamColor.BLACK && !blackPlayer) {
            throw new ServerException("Not black team's turn", 403);
        }

        // Make sure the game isn't in checkmate or stalemate
        boolean gameEnd = game.isInCheckmate(ChessGame.TeamColor.WHITE) || game.isInCheckmate(ChessGame.TeamColor.BLACK);
        boolean gameStalemate = game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInStalemate(ChessGame.TeamColor.BLACK);
        if (gameEnd || gameStalemate) {
            throw new ServerException("Game is over!", 400);
        }
        return true;
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
