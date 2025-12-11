package client;

import chess.*;
import client.websocket.GameMessageHandler;
import client.websocket.WebSocketClient;
import com.google.gson.Gson;
import exception.ResponseException;
import models.GameData;
import websocket.commands.UserGameCommand;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class GameClient implements Client, GameMessageHandler {
    static final String ANSI_RESET = "\u001B[0m";
    static final String WHITE_BG_COLOR = "\u001B[47m";
    static final String BLACK_BG_COLOR = "\u001B[100m";
    static final String HIGHLIGHT_BG = "\u001B[42m"; // green
    static final String SELECT_BG = "\u001B[43m";    // yellow
    static final String BORDER_COLOR = "\u001B[47m";

    private final ServerFacade server;
    private final String authToken;
    private GameData game;
    private final String teamColor;
    private final Gson gson = new Gson();
    private WebSocketClient wsClient;

    @Override
    public void onLoadGame(GameData game) {
        this.game = game;
        System.out.println("\n-=三=-  BOARD UPDATED  -=三=-\n");
        drawBoard();
    }

    @Override
    public void onNotification(String message) {
        System.out.println("Notification: " + message);
    }

    @Override
    public void onError(String error) {
        if (error.toLowerCase().contains("move") ||
                error.toLowerCase().contains("illegal") ||
                error.toLowerCase().contains("turn")) {
            if (error.toLowerCase().contains("error:")) {
                System.err.println(error);
            } else {
                System.err.println("Error: " + error);
            }
        }
    }

    // models for gson serialization
    private record MessageResponse(String message) {}

    /**
     * Constructor for the game client
     * @param server is the serverfacade; we only use this to get the list of games. Potential simplification exists.
     * @param authToken is the authToken for the client
     * @param gameID is the id of the game we're trying to represent
     * @param teamColor is the team the player is joining
     */
    public GameClient(ServerFacade server, String authToken, Integer gameID, String teamColor) {
        this.server = server;
        this.authToken = authToken;
        this.teamColor = teamColor;
        if (gameID != null) {
            game = findGame(gameID);
        }

        try {
            URI uri = URI.create("ws://localhost:8080/ws");
            wsClient = new WebSocketClient(uri, this);

            UserGameCommand connected = new UserGameCommand(
                    UserGameCommand.CommandType.CONNECT,
                    authToken,
                    gameID
            );
            wsClient.send(connected);
        } catch (Exception e) {
            // just learned about this println message
            System.err.println("Websocket connection failed: " + e.getMessage());
        }
        drawBoard();
    }

    @Override
    public String help() {
        return gson.toJson(new MessageResponse("""
        --- CHESS GAME HELP ---
        move e2 e4        - Make a move
        move g7 g8 q      - Make a move with pawn promotion (q,r,b,n)
        highlight e2      - Show legal moves (YELLOW = selected, GREEN = legal)
        redraw            - Redraw board
        resign            - Forfeit game (y/n to confirm)
        leave/exit/quit   - Exit to main menu
        help              - Show help message
        """));
    }

    @Override
    public String eval(String input) {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        try {
            return switch (cmd) {
                case "leave", "exit", "quit", "leave game", "exit game", "quit game":
                    yield leaveGame(params);
                case "resign":
                    yield resign(params);
                case "move":
                    yield makeMove(params);
                case "redraw":
                    yield redraw(params);
                case "highlight":
                    yield highlight(params);
                case "help":
                    yield help();
                default:
                    yield "{\"message\":\"Error: Unknown command. Type 'help' for a list of available commands.\"}";
            };
        } catch (ResponseException e) {
            return String.format("{\"status\":\"error\",\"message\":\"%s\"}", e.getMessage());
        }
    }

    /**
     * leaveGame sends a message that the REPL uses to exit the game
     * @param params is an array of parameters passed in. Should be empty.
     * @return the string containing a json formatted message and authtoken
     */
    private String leaveGame(String[] params) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken,
                    // this allows me to not have to spend several lines verifying that the game's id is not null
                    game != null ? game.gameID() : null);
            wsClient.send(command);
        } catch (Exception e) {
            System.err.println("LEAVE command not sent: " + e.getMessage());
        }
        return String.format("{\"message\":\"successfully exited game\", \"authToken\":\"%s\", \"leaveGame\":true}", authToken);
    }

    private String resign(String[] params) throws ResponseException {
        System.out.print("Are you sure you want to resign from the match? (y/n)\n>>");
        var response = System.console().readLine();
        // if the response is anything but 'n', we cancel the resignation.
        if (response == null || !response.equalsIgnoreCase("y")) {
            return gson.toJson(new MessageResponse("Resignation canceled!"));
        }
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN,
                    authToken,
                    game != null ? game.gameID() : null
            );
            wsClient.send(command);
            return gson.toJson(new MessageResponse("You have forfeited the game."));
        } catch (Exception e) {
            throw new ResponseException("Resignation command failed to send: " + e.getMessage(), 500);
        }
    }

    private String redraw(String[] params) {
        if (game != null) {
            drawBoard();
            return gson.toJson(new MessageResponse("Board redrawn!"));
        }
        return gson.toJson(new MessageResponse("Board does not exist..."));
    }

    private String highlight(String[] params) {
        boolean invalidParams = params.length != 1 || params[0].length() != 2 || validParams(params[0], "a1");
        if (invalidParams) {
            return gson.toJson(new MessageResponse("Usage: Highlight <piece coordinate> (example: 'highlight c2'"));
        }
        ChessPosition position = parsePosition(params[0]);
        ChessGame chessGame = game.getGame();
        ChessPiece piece = chessGame.getBoard().getPiece(position);

        if (piece == null) {
            return gson.toJson(new MessageResponse("No piece exists on square " + params[0]));
        }

        Collection<ChessMove> moves = chessGame.validMoves(position);
        if (moves == null || moves.isEmpty()) {
            return gson.toJson(new MessageResponse("No valid moves exist for piece occupying square " + params[0]));
        }

        drawHighlightBoard(position, moves);
        return gson.toJson(new MessageResponse(moves.size() + " legal moves for piece on square " + params[0] + " highlighted!"));
    }

    private void drawHighlightBoard(ChessPosition selected, Iterable<ChessMove> moves) {


        boolean isBlack = false;
        if (teamColor != null) {
            isBlack = teamColor.equalsIgnoreCase("black");
        }
        int rowIDStart = isBlack ? 1 : 8;
        int rowIDEnd = isBlack ? 9 : 0;
        int colIDStart = isBlack ? 8 : 1;
        int colIDEnd = isBlack ? 0 : 9;
        int rowStep = isBlack ? 1 : -1;
        int colStep = isBlack ? -1 : 1;

        ChessBoard board = game.getGame().getBoard();

        // Create an array of indexes to be highlighted. I started with an int array but a bool array made more sense.
        boolean[][] highlights = new boolean[9][9];
        for (ChessMove move : moves) {
            ChessPosition pos = move.getEndPosition();
            highlights[pos.getRow()][pos.getColumn()] = true;
        }

        printBorderEdge();
        for (int row = rowIDStart; row != rowIDEnd; row += rowStep) {
            System.out.print(BORDER_COLOR + " " + row + " " + ANSI_RESET);

            for (int col = colIDStart; col != colIDEnd; col += colStep) {
                // determines the checkerboard pattern
                boolean isWhiteSquare = (row + col) % 2 == 1;

                ChessPiece occupyingPiece = board.getPiece(new ChessPosition(row, col));

                String bgColor = isWhiteSquare ? WHITE_BG_COLOR : BLACK_BG_COLOR;
                if (highlights[row][col]) {
                    bgColor = HIGHLIGHT_BG;
                }
                if (selected.getRow() == row && selected.getColumn() == col) {
                    bgColor = SELECT_BG;
                }

                String piece = formatPieceText(occupyingPiece);
                System.out.print(bgColor + " " + piece + " " + ANSI_RESET);
            }
            System.out.println(BORDER_COLOR + " " + row + " " + ANSI_RESET);
        }
        // Print bottom row
        printBorderEdge();
        System.out.println("YELLOW = selected, GREEN = legal move");
    }

        /**
         * drawBoard will draw the entire chess board.
         */
    private void drawBoard () {
        // Figuring out which direction to go
        boolean isBlack = false;
        if (teamColor != null) {
            isBlack = teamColor.equalsIgnoreCase("black");
        }
        int rowIDStart = isBlack ? 1 : 8;
        int rowIDEnd = isBlack ? 9 : 0;
        int colIDStart = isBlack ? 8 : 1;
        int colIDEnd = isBlack ? 0 : 9;
        int rowStep = isBlack ? 1 : -1;
        int colStep = isBlack ? -1 : 1;

        ChessBoard board = game.getGame().getBoard();

        // Print top row
        printBorderEdge();

        // Print board: For each row, we subtract 1 or add 1 (depending on team color) until we reach the end.
        for (int row = rowIDStart; row != rowIDEnd; row += rowStep) {
            System.out.print(BORDER_COLOR + " " + row + " " + ANSI_RESET);

            for (int col = colIDStart; col != colIDEnd; col += colStep) {
                // determines the checkerboard pattern
                boolean isWhiteSquare = (row + col) % 2 == 1;

                ChessPiece occupyingPiece = board.getPiece(new ChessPosition(row, col));

                String bgColor = isWhiteSquare ? WHITE_BG_COLOR : BLACK_BG_COLOR;
                String piece = formatPieceText(occupyingPiece);
                System.out.print(bgColor + " " + piece + " " + ANSI_RESET);
            }
            System.out.println(BORDER_COLOR + " " + row + " " + ANSI_RESET);
        }
        // Print bottom row
        printBorderEdge();
    }

    private String makeMove (String[]params) throws ResponseException {
        String badCommand = "Command not recognized. Use 'move <from> <to> [promotion piece]' " +
                "(examples: 'move e2 e4' or 'move b2 b1 q')." +
                "\n    (Promotion piece only necessary for promoting pawns)";
        ChessPiece.PieceType promotionPiece = null;

        if (params.length < 2 || validParams(params[0], params[1])) {
            return gson.toJson(new MessageResponse(badCommand));
        }
        if (game == null) {
            return gson.toJson(new MessageResponse("No game currently active"));
        }

        ChessPosition from = parsePosition(params[0]);
        ChessPosition to = parsePosition(params[1]);

        if (params.length == 3) {
            promotionPiece = parsePromotion(params[2], to.getRow());
        }

        ChessMove move = new ChessMove(from, to, promotionPiece);
        try {
            UserGameCommand command = new UserGameCommand(
                    UserGameCommand.CommandType.MAKE_MOVE,
                    authToken,
                    game.gameID(),
                    move
            );

            wsClient.send(command);
            return gson.toJson(new MessageResponse(""));
        } catch (Exception e) {
            throw new ResponseException("makeMove failure: " + e.getMessage(), 500);
        }
    }

    private ChessPiece.PieceType parsePromotion (String promo,int row) throws ResponseException {
        return switch (promo.toLowerCase()) {
            case "q", "queen" -> ChessPiece.PieceType.QUEEN;
            case "r", "rook" -> ChessPiece.PieceType.ROOK;
            case "b", "bishop" -> ChessPiece.PieceType.BISHOP;
            case "n", "knight" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new ResponseException(
                    "Valid promotion inputs are: q/r/b/n (queen/rook/bishop/knight)", 400);
        };
    }

    private boolean validParams(String to, String from) {
        if (to.length() != 2 || from.length() != 2) {
            return true;
        }
        if (to.charAt(0) < 'a' || to.charAt(0) > 'h' || from.charAt(0) < 'a' || from.charAt(0) > 'h') {
            return true;
        }
        if (to.charAt(1) < '1' || to.charAt(1) > '8' || from.charAt(1) < '1' || from.charAt(1) > '8') {
            return true;
        }
        return false;
    }

    private ChessPosition parsePosition (String pos){
        int col = pos.charAt(0) - 'a' + 1;
        int row = pos.charAt(1) - '0';
        return new ChessPosition(row, col);
    }

    /**
     * Helper function to print the edges of the border
     */
    private void printBorderEdge () {
        System.out.print("\u001B[47m" + "   ");
        if (teamColor != null && teamColor.equalsIgnoreCase("black")) {
            for (char col = 'h'; col >= 'a'; col--) {
                System.out.print(" " + col + " ");
            }
        } else {
            //learned you can do this with chars, which is kinda cool
            for (char col = 'a'; col <= 'h'; col++) {
                System.out.print(" " + col + " ");
            }
        }
        System.out.println("\u001B[47m" + "   " + "\u001b[0m");
    }

    /**
     * Helper function to format the piece text appropriately for display
     * @param occupyingPiece is the piece we're getting the text representation for
     * @return the text representation of the occupying piece
     */
    private static String formatPieceText (ChessPiece occupyingPiece){
        String piece = " ";

        if (occupyingPiece != null) {
            int isWhite = occupyingPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : 0;
            piece = isWhite == 1 ? "\u001B[94m" : "\u001B[38;5;52m";

            switch (occupyingPiece.getPieceType()) {
                case PAWN -> piece = piece + UnicodePieces.PIECES.getPiece(6 * isWhite);
                case ROOK -> piece = piece + UnicodePieces.PIECES.getPiece(1 + 6 * isWhite);
                case KNIGHT -> piece = piece + UnicodePieces.PIECES.getPiece(2 + 6 * isWhite);
                case BISHOP -> piece = piece + UnicodePieces.PIECES.getPiece(3 + 6 * isWhite);
                case QUEEN -> piece = piece + UnicodePieces.PIECES.getPiece(4 + 6 * isWhite);
                case KING -> piece = piece + UnicodePieces.PIECES.getPiece(5 + 6 * isWhite);
            }
        }
        return piece;
    }

    /**
     * Helper function to find the specific game by ID
     * @param gameID is the id of the game we're looking for
     * @return the game in the gameList of gameID "gameID"
     */
    private GameData findGame ( int gameID){
        try {
            List<GameData> gameList = server.listGame(authToken);
            for (GameData game : gameList) {
                if (game.gameID() == gameID) {
                    return game;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}