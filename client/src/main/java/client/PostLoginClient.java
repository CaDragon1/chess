package client;

import chess.ChessGame;
import exception.ResponseException;
import models.GameData;

import java.util.Arrays;
import java.util.List;

public class PostLoginClient implements Client{
    private final ServerFacade server;
    private final String authToken;
    private List<GameData> cachedGames = null;

    public PostLoginClient(ServerFacade server, String authToken) {
        this.server = server;
        this.authToken = authToken;
    }

    @Override
    public String help() {
        return "--- HELP ---\nCommands:\nlist games\ncreate game <game name>\njoin game <game number> <team color>" +
                "\nobserve game <game number>\nhelp\nlogout";
    }

    @Override
    public String eval(String input) throws ResponseException {
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
            default:
                yield "Error: Unknown command. Type 'help' for a list of available commands.";
        };
    }

    private String observeGame(String[] params){
        try {
            int gameIndex;
            try {
                gameIndex = Integer.parseInt(params[1]);
            } catch (Exception e) {
                return "Error: Invalid game number";
            }
            int gameID = getGameID(gameIndex);

            List<GameData> gameList = server.listGame(authToken);
            boolean contains = (findGame(gameID) != null);
            if (!contains) {
                return "Error: Invalid game";
            }
            return String.format("{\"status\":\"success\", \"message\":\"%s joining game as observer...\", \"" +
                    "authToken\":\"%s\", \"gameID\":\"%s\"}", params[0], authToken, params[1]);
        } catch (Exception e) {
            return ("Unknown error: " + e.getMessage());
        }
    }

    // Helper function to find the specific game by ID
    private GameData findGame(int gameID) throws ResponseException {
        List<GameData> gameList = server.listGame(authToken);
        for (GameData game : gameList) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
    }

    private String joinGame(String[] params) throws ResponseException {
        if (params.length < 2) {
            return "Game not joined: Try 'join game <game ID> <team color>'";
        }

        int gameIndex;
        try {
            gameIndex = Integer.parseInt(params[0]);
        } catch (Exception e) {
            return "Game not joined: Game number must be an integer";
        }
        String color = params[1];

        // Save the gameID the user is attempting to join.
        int gameID = getId(gameIndex);

        ChessGame.TeamColor teamColor;
        GameData chessGame = findGame(gameID);
        if (chessGame == null) {
            return "Error: No game of index " + gameIndex + " found.";
        }

        if (color.equalsIgnoreCase("white")) {
            if (chessGame.whiteUsername() == null) {
                teamColor = ChessGame.TeamColor.WHITE;
            } else {
                return "Game not joined: White team already occupied.";
            }
        }
        else if (color.equalsIgnoreCase("black")) {
            if (chessGame.blackUsername() == null) {
                teamColor = ChessGame.TeamColor.BLACK;
            } else {
                return "Game not joined: Black team already occupied.";
            }
        } else {
            return "Game not joined: Team color must be white or black.";
        }
        server.joinGame(authToken, teamColor, gameID);
        return String.format("{\"status\":\"success\", \"message\":\"%s joining game as %s...\", \"" +
                "authToken\":\"%s\", \"gameID\":\"%s\"}", params[0], teamColor, authToken, params[1]);
    }

    private int getId(int gameIndex) throws ResponseException {
        boolean precached = true;
        if (cachedGames == null) {
            cachedGames = server.listGame(authToken);
            precached = false;
        }
        int gameID = getGameID(gameIndex);
        if (precached) {
            cachedGames = server.listGame(authToken);
        }
        return gameID;
    }

    private int getGameID(int gameIndex) {
        GameData[] gameArray = cachedGames.toArray(new GameData[0]);
        return gameArray[gameIndex - 1].gameID();
    }

    private String createGame(String[] params) {
        if (params.length < 1 || params[0].isBlank()) {
            return "Game not created: Must provide a name for the game";
        }
        try {
            int gameID = server.createGame(authToken, params[0]);
            return "Game " + gameID + " created! Use 'join game <gameID> <team color>' to join.";
        } catch (Exception e) {
            return "Error: Game creation failed --> " + e.getMessage();
        }
    }

    private String logout(String[] params) {
        try {
            server.logoutUser(authToken);
            return "Successfully logged out.";
        } catch (Exception e) {
            return "Error: Logout unsuccessful --> " +  e.getMessage();
        }
    }

    private String listGames() throws ResponseException {
        List<GameData> games = server.listGame(authToken);
        if (games.isEmpty()) {
            return "No games found";
        }

        StringBuilder gameList = new StringBuilder("--- GAMES ---\nIndex    Name    White Username    Black Username\n");
        int index = 1;
        for (GameData game : games) {
            gameList.append(index).append("  ").append(game.gameName()).append("  ")
                    .append(game.whiteUsername()).append("    ").append(game.blackUsername()).append("\n");
            index++;
        }
        return gameList.toString();
    }

}
