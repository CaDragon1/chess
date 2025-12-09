package client;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import models.GameData;

import java.util.Arrays;
import java.util.List;

public class PostLoginClient implements Client{
    private final ServerFacade server;
    private final String authToken;
    private List<GameData> cachedGames = null;
    private final Gson gson = new Gson();

    // models for gson serialization
    private record MessageResponse(String message) {}
    private record StatusResponse(String status, String message) {}
    private record ObserveResponse(String status, String message, String authToken, int gameID) {}
    private record JoinResponse(String status, String message, String authToken, int gameID, String teamColor) {}

    /**
     * PostLoginClient constructor to handle the pre-game and post-login menu functions
     * @param server is the serverfacade containing the game info and more
     * @param authToken is the auth token of the user
     */
    public PostLoginClient(ServerFacade server, String authToken) {
        this.server = server;
        this.authToken = authToken;
    }

    @Override
    public String help() {
        var msg = """
                --- HELP ---
                Commands:
                list games
                create game <game name>
                join game <game number> <team color>
                observe game <game number>
                help
                logout""";
        return gson.toJson(new MessageResponse(msg));
    }

    @Override
    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch(cmd) {
                case "list":
                    if (tokens.length > 1 && tokens[1].equals("games")) yield listGames();
                case "logout":
                    yield logout(params);
                case "create":
                    if (tokens.length > 1 && tokens[1].equals("game")) yield createGame(params);
                case "join":
                    if (tokens.length > 1 && tokens[1].equals("game")) yield joinGame(params);
                case "observe":
                    if (tokens.length > 1 && tokens[1].equals("game")) yield observeGame(params);
                case "help":
                    yield help();
                case "thatsaspicymeatball":
                    yield clearDatabase();
                default:
                    yield gson.toJson(new MessageResponse(
                            "Error: Unknown command. Type 'help' for a list of available commands."));
            };
        } catch (ResponseException e) {
            return gson.toJson(new StatusResponse("error", e.getMessage()));
        }
    }

    /**
     * Method to handle an observeGame command
     * @param params contains game index
     * @return a string with the success or error state; used by repl to execute client state changes or commands
     */
    private String observeGame(String[] params){
        try {
            int gameIndex;
            try {
                gameIndex = Integer.parseInt(params[1]);
            } catch (Exception e) {
                return gson.toJson(new MessageResponse("Error: Invalid game number"));
            }
            if (cachedGames == null) {
                cachedGames = server.listGame(authToken);
            }
            int gameID = findGameID(gameIndex);

            boolean contains = (findGame(gameID) != null);
            if (!contains) {
                return gson.toJson(new MessageResponse("Error: Invalid game"));
            }
            return gson.toJson(new ObserveResponse(
                    "success", "Observing game...", authToken, gameID
            ));

        } catch (Exception e) {
            if (e.getMessage().contains("out of bounds")) {
                return gson.toJson(new MessageResponse("Error: No game found of specified index"));
            }
            return gson.toJson(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    /**
     * Helper function to find the specific game by ID
     * @param gameID is the game id
     * @return the GameData of the game with game ID gameID
     * @throws ResponseException if there's an error getting the information
     */
    private GameData findGame(int gameID) throws ResponseException {
        List<GameData> gameList = server.listGame(authToken);
        for (GameData game : gameList) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    /**
     * Method to handle joining a game. This one's beefy; could use breaking apart.
     * @param params
     * @return a json formatted string with the success or failure message for the REPL
     * @throws ResponseException if there is an error in any of the subfunctions
     */
    private String joinGame(String[] params) throws ResponseException {
        if (params.length < 3) {
            return gson.toJson(new MessageResponse(
                    "Game not joined: Try 'join game <game ID> <team color>'"));
        }
        if (params.length > 3) {
            throw new ResponseException("Game not joined: Too many parameters", 400);
        }

        int gameIndex;
        try {
            gameIndex = Integer.parseInt(params[1]);
        } catch (Exception e) {
            return gson.toJson(new MessageResponse(
                    "Game not joined: Game number must be an integer"));
        }
        String color = params[2];

        // Save the gameID the user is attempting to join.
        int gameID = findGameID(gameIndex);

        ChessGame.TeamColor teamColor;
        GameData chessGame = findGame(gameID);
        if (chessGame == null) {
            return gson.toJson(new MessageResponse(
                    String.format("Error: No game of index %s found.", gameIndex)));
        }

        if (color.equalsIgnoreCase("white")) {
            if (chessGame.whiteUsername() == null) {
                teamColor = ChessGame.TeamColor.WHITE;
            } else {
                return gson.toJson(new MessageResponse(
                        "Game not joined: White team already occupied."));            }
        }
        else if (color.equalsIgnoreCase("black")) {
            if (chessGame.blackUsername() == null) {
                teamColor = ChessGame.TeamColor.BLACK;
            } else {
                return "{\"message\":\"Game not joined: Black team already occupied.\"}";
            }
        } else {
            return "{\"message\":\"Game not joined: Team color must be white or black.\"}";
        }
        server.joinGame(authToken, teamColor, gameID);
        return String.format("{\"status\":\"success\", \"message\":\"Joining game as %s...\", \"" +
                "authToken\":\"%s\", \"gameID\":\"%s\", \"teamColor\":\"%s\"}", teamColor, authToken, gameID, teamColor);
    }

    /**
     * findGameID is a helper method to assist in getting the id of a game from the game list and handle the exceptions thereof.
     * @param gameIndex is the index of the game we're looking at
     * @return the gameID of the indexed game
     * @throws ResponseException e
     */
    private int findGameID(int gameIndex) throws ResponseException {
        int gameID;
        try {
            boolean precached = true;
            if (cachedGames == null) {
                cachedGames = server.listGame(authToken);
                precached = false;
            }
            GameData[] gameArray = cachedGames.toArray(new GameData[0]);
            gameID = gameArray[gameIndex - 1].gameID();
            if (precached) {
                cachedGames = server.listGame(authToken);
            }
        } catch (Exception e) {
            if (e.getMessage().contains("out of bounds")){
                throw new ResponseException("No game of index " + gameIndex + " exists.", 400);
            }
            throw new ResponseException("Error: " + e.getMessage(), 500);
        }
        return gameID;
    }

    /**
     * Function to prompt game creation
     * @param params contains the game name and the auth token
     * @return a json-formatted string telling the REPL how to proceed depending on if game was created or not
     */
    private String createGame(String[] params) {
        if (params.length < 2 || params[1].isBlank()) {
            return "{\"message\":\"Game not created: Must provide a name for the game\"}";
        }
        try {
            server.createGame(authToken, params[1]);
            return String.format("{\"message\":\"Game %s created! Use 'join game <game index> <team color>' to join.\"}", params[1]);
        } catch (Exception e) {
            return String.format("{\"message\":\"Error: Game creation failed --> %s\"}", e.getMessage());
        }
    }

    /**
     * Function to indicate the user wants to be logged out
     * @param params contains nothing in this case
     * @return the json-formatted string telling the repl if the logout works or doesn't
     */
    private String logout(String[] params) {
        try {
            server.logoutUser(authToken);
            return "{\"message\":\"Successfully logged out.\"}";
        } catch (Exception e) {
            return String.format("{\"message\":\"Error: Logout unsuccessful --> %s\"}", e.getMessage());
        }
    }

    /**
     * Function to list games
     * @return a json-formatted list of games for display
     * @throws ResponseException if server.listGame fails
     */
    private String listGames() throws ResponseException {
        List<GameData> games = server.listGame(authToken);
        if (games.isEmpty()) {
            return "{\"message\":\"No games found\"}";
        }

        StringBuilder gameList = new StringBuilder("{\"message\":\"--- GAMES ---\\nIndex    Name    White Username    Black Username\\n");
        int index = 1;
        for (GameData game : games) {
            gameList.append(index).append("  ").append(game.gameName()).append("  ")
                    .append(game.whiteUsername()).append("    ").append(game.blackUsername()).append("\\n");
            index++;
        }
        gameList.append("\"}");
        return gameList.toString();
    }

    /**
     * Function to clear a database. Should not, ultimately, be user-usable.
     * @return json-formatted message indicating clear success
     * @throws ResponseException if logout or database clearing fails
     */
    private String clearDatabase() throws ResponseException {
        server.logoutUser(authToken);
        server.clearDatabase();
        return "{\"message\":\"Successfully logged out.\"}";
    }

}
