import chess.ChessGame;
import com.sun.nio.sctp.NotificationHandler;
import exception.ResponseException;
import models.AuthTokenData;
import models.GameData;
import models.UserData;
import server.ServerFacade;
import ui.DataCache;
import ui.EscapeSequences;

import java.util.Collection;

public class ChessClient {
    private final ServerFacade server;
    private final String serverURL;
    private final NotificationHandler notificationHandler;
    private Boolean isLoggedIn;
    private DataCache dataCache;

    // Set up client server connection
    public ChessClient(String serverURL, NotificationHandler notificationHandler) {
        this.serverURL = serverURL;
        server = new ServerFacade(this.serverURL);
        this.notificationHandler = notificationHandler;
        isLoggedIn = Boolean.FALSE;
        dataCache = new DataCache();
    }

    public String eval(String input) {
        try {
            // Read and separate tokens, commands, and parameters
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            return switch (cmd) {
                case "register" -> register(tokens);
                case "login" -> login(tokens);
                case "list" -> listGames(tokens);
                case "create" -> createGame(tokens);
                case "join" -> joinGame(tokens);
                case "observe" -> observeGame(tokens);
                case "logout" -> logout();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException e) {
            return "error: " + e.getMessage();
        }
    }

    private String register(String... parameters) throws ResponseException {
        validateParameterLength(parameters, 3);

        UserData user = new UserData(parameters[1], parameters[2], parameters[3]);

        // Set auth token in cached data object
        AuthTokenData authTokenData = server.registerUser(user);
        dataCache.setAuthToken(authTokenData.authToken());

        isLoggedIn = Boolean.TRUE;
        return user.username() + " has been successfully registered!";
    }

    private String login(String... parameters) throws ResponseException {
        validateParameterLength(parameters, 2);
        AuthTokenData authTokenData = server.loginUser(parameters[1], parameters[2]);
        dataCache.setAuthToken(authTokenData.authToken());
        isLoggedIn = Boolean.TRUE;
        return parameters[1] + " has been successfully logged in!";
    }

    private String listGames(String... parameters) throws ResponseException {
        validateParameterLength(parameters, 1);

        Collection<GameData> gameList = server.listGame(parameters[1]);
        dataCache.setGameCache(gameList);

        // Create string display result using StringBuilder
        StringBuilder resultString = new StringBuilder();
        resultString.append(EscapeSequences.ERASE_SCREEN);
        resultString.append(EscapeSequences.SET_TEXT_BOLD).append("\n --< Active Games >--\n")
                .append(EscapeSequences.RESET_TEXT_BOLD_FAINT);

        int index = 1;
        for (GameData game : gameList) {
            // Placeholders to insert necessary information, unfortunately creates a really annoying run-on string.
            // Might want to reimplement later for more conciseness
            resultString.append(String.format("%s%d.%s %s%s%s [ White: %s%s%s | Black: %s%s%s ]\n",
                    EscapeSequences.SET_TEXT_COLOR_BLUE, index++, EscapeSequences.RESET_TEXT_COLOR,
                    EscapeSequences.SET_TEXT_BOLD, game.gameName(), EscapeSequences.RESET_TEXT_BOLD_FAINT,
                    EscapeSequences.SET_TEXT_COLOR_WHITE, game.whiteUsername(), EscapeSequences.RESET_TEXT_COLOR,
                    EscapeSequences.SET_BG_COLOR_DARK_GREY, game.blackUsername(), EscapeSequences.RESET_TEXT_COLOR));
        }
        return resultString.toString();
    }

    // parameters only need to be a game name. AuthToken is stored in the DataCache.
    private String createGame(String... parameters) throws ResponseException {
        validateParameterLength(parameters, 1);

        int gameID = server.createGame(dataCache.getAuthToken(), parameters[1]);

        // Create string display result using StringBuilder
        StringBuilder resultString = new StringBuilder();
        return "Successful game creation with ID " + gameID;
    }

    // parameters[1] is the team color, and parameters[2] is the gameID
    private String joinGame(String... parameters) throws ResponseException {
        // Determine team color
        ChessGame.TeamColor teamColor;
        if (parameters[1].contains("white") || parameters[1].contains("WHITE")) {
            teamColor = ChessGame.TeamColor.WHITE;
        }
        else if (parameters[1].contains("black") || parameters[1].contains("BLACK")) {
            teamColor = ChessGame.TeamColor.BLACK;
        }
        else {
            teamColor = null;
        }
        // Find the gameID based on our cacheData number system
        GameData gameData = dataCache.getGameByIndex(Integer.parseInt(parameters[2]));

        server.joinGame(dataCache.getAuthToken(), teamColor, gameData.gameID());

        StringBuilder resultString = new StringBuilder();
        resultString.append(String.format("Game - %s\nWhite - %s%s%s\nBlack - %s%s%s",
                EscapeSequences.SET_TEXT_COLOR_BLUE, gameData.gameName(), EscapeSequences.RESET_TEXT_COLOR,
                EscapeSequences.SET_TEXT_COLOR_WHITE, gameData.whiteUsername(), EscapeSequences.RESET_TEXT_COLOR,
                EscapeSequences.SET_BG_COLOR_DARK_GREY, gameData.blackUsername(), EscapeSequences.RESET_TEXT_COLOR));
        return resultString.toString();
    }

    private String

    private void validateParameterLength(String[] params, int expectedLength) throws ResponseException {
        if (params.length < expectedLength) {
            throw new ResponseException("Parameters missing", 400);
        }
        else if (params.length > expectedLength) {
            throw new ResponseException("Too many parameters given", 400);
        }
    }
}
