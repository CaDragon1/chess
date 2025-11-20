package client;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import exception.ResponseException;
import models.GameData;

import java.util.Arrays;
import java.util.List;

public class GameClient implements Client {
    private ServerFacade server;
    private String authToken;
    private GameData game;
    private String teamColor;

    public GameClient(ServerFacade server, String authToken, Integer gameID, String teamColor) throws ResponseException {
        this.server = server;
        this.authToken = authToken;
        this.teamColor = teamColor;
        if (gameID != null) {
            game = findGame(gameID);
        }
    }

    @Override
    public String help() {
        return "";
    }

    @Override
    public String eval(String input) throws ResponseException {
        var tokens = input.toLowerCase().split(" ");
        var cmd = (tokens.length > 0) ? tokens[0] : "help";
        var params = Arrays.copyOfRange(tokens, 1, tokens.length);
        return switch(cmd) {
            case "leave", "exit", "quit", "leave game", "exit game", "quit game":
                yield leaveGame(params);
            case "help":
                yield help();
            default:
                yield "Error: Unknown command. Type 'help' for a list of available commands.";
        };
    }

    private String leaveGame(String[] params) {
        return "Successfully exited game";
    }

    private void drawBoard() {
        // Using ANSI codes for colors
        final String ANSI_RESET = "\u001B[0m";
        final String WHITE_BG_COLOR = "\u001B[47m";
        final String BLACK_BG_COLOR = "\u001B[100m";
        final String BORDER_COLOR = "\u001B[47m";

        // Figuring out which direction to go
        boolean isBlack = teamColor.equalsIgnoreCase("black");
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

    /**
     * Helper function to print the edges of the border
     */

    private void printBorderEdge() {
        System.out.print("\u001B[47m" + "   ");
        if (teamColor.equalsIgnoreCase("black")) {
            for (char col = 'h'; col >= 'a'; col--) {
                System.out.print(" " + col + " ");
            }
        } else {
            //learned you can do this with chars, which is kinda cool
            for (char col = 'a'; col <= 'h'; col++) {
                System.out.print(" " + col + " ");
            }
        }
        System.out.println("\u001B[0m");
    }

    // Helper function to format the piece text appropriately
    private static String formatPieceText(ChessPiece occupyingPiece) {
        String piece = " ";

        if (occupyingPiece != null) {
            int isWhite = occupyingPiece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : 0;
            piece = isWhite == 1 ? "\u001B[94m" : "\u001B[31m";

            switch(occupyingPiece.getPieceType()) {
                case PAWN -> piece = piece + String.valueOf(UnicodePieces.PIECES.getPiece(8 * isWhite));
                case ROOK -> piece = piece + String.valueOf(UnicodePieces.PIECES.getPiece(1 + 8 * isWhite));
                case KNIGHT -> piece = piece + String.valueOf(UnicodePieces.PIECES.getPiece(2 + 8 * isWhite));
                case BISHOP -> piece = piece + String.valueOf(UnicodePieces.PIECES.getPiece(3 + 8 * isWhite));
                case QUEEN -> piece = piece + String.valueOf(UnicodePieces.PIECES.getPiece(4 + 8 * isWhite));
                case KING -> piece = piece + String.valueOf(UnicodePieces.PIECES.getPiece(5 + 8 * isWhite));
            }
        }
        return piece;
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

}
