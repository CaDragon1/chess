package chess;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard gameBoard;
    private TeamColor currentTeamTurn;

    public ChessGame() {
        gameBoard = new ChessBoard();
        currentTeamTurn = TeamColor.WHITE;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
    }

    private Collection<ChessMove> allTeamMoves(TeamColor teamColor) {
        int startingIndex = (teamColor == TeamColor.WHITE ? 0 : 6);
        ChessPiece checkingPiece;
        long[] gameBitboards = gameBoard.getBitboards();
        Collection<ChessMove> possibleMoves = new HashSet<>();
        ChessPosition checkPosition;

        for (int i = startingIndex; i < startingIndex + 6; i++) {
            long bitboard = gameBitboards[i];
            checkingPiece = determinePiece(i, teamColor);
            // I got this code idea from https://alexharri.com/blog/bit-set-iteration; I'm trying to learn bitwise
            // operators, and two's complement makes this an efficient way of cycling through only our needed bits.
            // Took a while for me to understand the premise, though.
            while (bitboard != 0) {
                // lsb == least significant bit
                int lsb = (Long.numberOfTrailingZeros(bitboard & -bitboard));
                checkPosition = new ChessPosition(lsb);
                possibleMoves.addAll(checkingPiece.pieceMoves(gameBoard, checkPosition));
                bitboard ^= (1L << lsb);
            }
        }
        return possibleMoves;
    }

    private ChessPiece determinePiece(int index, TeamColor teamColor) {
        return switch (index % 6) {
            case 0 -> new ChessPiece(teamColor, ChessPiece.PieceType.PAWN);
            case 5 -> new ChessPiece(teamColor, ChessPiece.PieceType.ROOK);
            case 4 -> new ChessPiece(teamColor, ChessPiece.PieceType.KNIGHT);
            case 3 -> new ChessPiece(teamColor, ChessPiece.PieceType.BISHOP);
            case 2 -> new ChessPiece(teamColor, ChessPiece.PieceType.QUEEN);
            case 1 -> new ChessPiece(teamColor, ChessPiece.PieceType.KING);
            default -> throw new IllegalStateException("Unexpected value: " + index % 6);
        };
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        int kingBBIndex = (teamColor == TeamColor.WHITE ? 5 : 11);
        long kingBitboard = gameBoard.getBitboards()[kingBBIndex];
        int kingIndex = Long.numberOfTrailingZeros(kingBitboard & -kingBitboard);

        Collection<ChessMove> enemyMoves = allTeamMoves(teamColor == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        for (ChessMove checkMove : enemyMoves) {
            if (checkMove.getEndPosition().getIndex() == kingIndex) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard.copy(board);
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }
}
