package service;

import chess.ChessGame;
import chess.ChessMove;
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

    /**
     * Constructor for gameservice
     * @param gameDAO is the gameDAO object with all the gamedata stored
     * @param authDAO is the authDAO object with all authdata stored
     */
    public GameService(GameDataAccess gameDAO, AuthDataAccess authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    /**
     * Function to create a game in the dao
     * @param authToken is the user token to verify authenticity
     * @param gameName is the name of the game being created
     * @return the gameid of the created game
     * @throws ServerException if the game creation fails or is unauthorized
     */
    public int createGame(String authToken, String gameName) throws ServerException {
        try {
            AuthData authData = authDAO.getAuthData(authToken);
            if (authData != null) {
                int gameID = generateGameID();
                // verify that there's no duplicate game id
                while (gameDAO.getGame(gameID) != null) {
                    gameID = generateGameID();
                }
                GameData game = new GameData(gameID, null, null, gameName,
                        new ChessGame(), GameData.GameStatus.PREGAME);
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

    /**
     * Function to join a game already existing in the dao
     * @param authData is used to get the authtoken for verification and username for adding
     * @param team is the team color the user is trying to join
     * @param gameID is the id of the game being joined
     * @throws ServerException upon unauthorized access or bad requests
     */
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

            if (isGameOver(game.status())) {
                throw new ServerException("cannot join completed game", 400);
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

    /**
     * Helper function for joinGame that sets the username in the gamedata object to the requested team
     * @param team is the team the user is trying to join
     * @param game is the gamedata representing the game being joined
     * @param auth is the authtoken for verification
     * @return gamedata object with updated username
     * @throws ServerException if there is a bad request or the team is occupied
     */
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

    /**
     * Function to fetch a list of all games in the database
     * @param authToken is the authtoken to verify access can be granted
     * @return a list of gamedata objects
     * @throws ServerException if unauthorized or if other issues arise
     */
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

    /**
     * Function to obtain a gamedata object by gameid (used by websocket)
     * @param gameID is the id of our desired game
     * @return the gamedata object from gameDAO
     * @throws ServerException if game is not found or other errors occur
     */
    public GameData getGame(int gameID) throws ServerException {
        try {
            return gameDAO.getGame(gameID);
        } catch (DataAccessException e) {
            throw new ServerException("Game not found: " + e.getMessage(), 404);
        }
    }

    /**
     * Function to make a move
     * @param authToken is the token of the player
     * @param gameID is the id of the game we're moving in
     * @param move is the move being attempted
     * @return the new gamedata object after the move is made
     * @throws ServerException if anything goes wrong with verification, viability, or other.
     */
    public GameData makeMove(String authToken, int gameID, ChessMove move) throws ServerException {
        try {
            AuthData auth = authDAO.getAuthData(authToken);
            if (auth == null) {
                throw new ServerException("unauthorized", 401);
            }
            GameData gameData = gameDAO.getGame(gameID);
            if (gameData == null) {
                throw new ServerException("bad request", 400);
            }

            ChessGame game = gameData.game();
            ChessGame.TeamColor currentTeam = game.getTeamTurn();

            if (!isActiveGame(gameData.status())) {
                throw new ServerException("GAME NOT LIVE; move not made. game status: " + gameData.status(), 403);
            }

            game.makeMove(move);
            GameData.GameStatus newGameStatus = getGameStatus(game, gameData);
            GameData updatedGame = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(),
                    gameData.gameName(), game, newGameStatus);

            gameDAO.updateGame(updatedGame);
            return updatedGame;

        } catch (DataAccessException e) {
            throw new ServerException("bad request", 400);
        } catch (InvalidMoveException e) {
            throw new ServerException("illegal move", 403);
        }
    }

    /**
     * Function to obtain the status of a game and determine changes to that status
     * @param game is the game we're getting the status of; may be a future gamestate or the current gamestate
     * @param currentData is the full gamedata, which may or may not contain "game"; it is used to verify game info
     * @return the status that corresponds with the current gamestate; stalemate, black_win, white_win, pregame,
     *  resigned, or live
     */
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

    public GameData resignGame(String authToken, int gameID) throws ServerException {
        try {
            AuthData auth = authDAO.getAuthData(authToken);
            if (auth == null) {
                throw new ServerException("unauthorized", 401);
            }
            GameData gameData = gameDAO.getGame(gameID);
            if (gameData == null) {
                throw new ServerException("bad request", 400);
            }

            boolean isInGame = auth.username().equals(gameData.blackUsername()) ||
                    auth.username().equals(gameData.whiteUsername());
            if (!isInGame) {
                throw new ServerException("player is observer and cannot resign", 403);
            }

            if (isActiveGame(gameData.status())) {
                GameData updatedGame = new GameData(
                        gameID,
                        gameData.whiteUsername(),
                        gameData.blackUsername(),
                        gameData.gameName(),
                        gameData.getGame(),
                        GameData.GameStatus.RESIGNED
                );
                gameDAO.updateGame(updatedGame);
                return updatedGame;
            } else {
                throw new ServerException("game is already over", 403);
            }
        } catch (DataAccessException e) {
            throw new ServerException("bad request", 400);
        }
    }

    /**
     * Generates a random game ID
     * @return the generated gameID
     */
    private int generateGameID() {
        Random rand = new Random();
        int id = rand.nextInt();
        if (id < 0) {
            id*=-1;
        }
        return id;
    }

    private boolean isActiveGame(GameData.GameStatus status) {
        return status == GameData.GameStatus.LIVE;
    }

    private boolean isGameOver(GameData.GameStatus status) {
        return switch(status) {
            case RESIGNED, BLACK_WIN, WHITE_WIN, STALEMATE -> true;
            default -> false;
        };
    }
}
