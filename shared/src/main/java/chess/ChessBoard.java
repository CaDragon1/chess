package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    // Make constants for each piece's bitboard index
    static final int WHITE_PAWNS = 0;
    static final int WHITE_ROOKS = 1;
    static final int WHITE_KNIGHTS = 2;
    static final int WHITE_BISHOPS = 3;
    static final int WHITE_QUEENS = 4;
    static final int WHITE_KINGS = 5;
    static final int BLACK_PAWNS = 6;
    static final int BLACK_ROOKS = 7;
    static final int BLACK_KNIGHTS = 8;
    static final int BLACK_BISHOPS = 9;
    static final int BLACK_QUEENS = 10;
    static final int BLACK_KINGS = 11;

    /** Create each bitboard as our board representation.
     * Every time we want to access a certain bitboard, we use the bitboard index for that piece.
     * example: bitboards[WHITE_PAWNS]
     * This should be intuitive and easy for me to read so that I don't get confused :D
     */
    private long[] bitboards = new long[12];
    private int enPassant;

    public ChessBoard() {
        emptyBoard();
        enPassant = -1;
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        int pieceIndex = position.getIndex();
        int boardIndex = findIndexByPiece(piece);
        if (boardIndex != -1) {
            bitboards[boardIndex] |= 1L << pieceIndex;
        }
        else {
            System.out.println("Invalid piece attempted to be added");
        }
    }

    /**
     * findIndexByPiece finds the bitboard index based on the piece type and color given.
     * @param piece is the piece given
     * @return whichever index corresponds to the correct bitboard in array bitboards
     */
    private int findIndexByPiece(ChessPiece piece) {
        if (piece == null) {
            return -1;
        }
        int teamModifier = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? 0 : 6;
        return switch (piece.getPieceType()) {
            case PAWN -> teamModifier;
            case ROOK -> 1 + teamModifier;
            case KNIGHT -> 2 + teamModifier;
            case BISHOP -> 3 + teamModifier;
            case QUEEN -> 4 + teamModifier;
            case KING -> 5 + teamModifier;
            case null, default -> -1;
        };
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {

        int index = position.getIndex();
        for (int i = WHITE_PAWNS; i <= BLACK_KINGS; i++) {
            if (((bitboards[i] >> index) & 1L) == 1) {
                return findPieceByIndex(i);
            }
        }
        return null;
    }

    /**
     * Gets a chess piece on the chessboard using the index
     *
     * @param index The index number to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(int index) {

        for (int i = WHITE_PAWNS; i <= BLACK_KINGS; i++) {
            if (((bitboards[i] >> index) & 1L) == 1) {
                return findPieceByIndex(i);
            }
        }
        return null;
    }

    /**
     * findPieceByIndex analyzes the bitboard index and returns a ChessPiece that corresponds to that bitboard.
     * @param bitboardIndex is the index of the bitboard stored in array bitboards.
     * @return the appropriate ChessPiece indicated by the index.
     */
    private ChessPiece findPieceByIndex(int bitboardIndex) {
        ChessGame.TeamColor color = (bitboardIndex < 6) ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        int pieceIndex = bitboardIndex % 6;

        return switch (pieceIndex) {
            case 0 -> new ChessPiece(color, ChessPiece.PieceType.PAWN);
            case 1 -> new ChessPiece(color, ChessPiece.PieceType.ROOK);
            case 2 -> new ChessPiece(color, ChessPiece.PieceType.KNIGHT);
            case 3 -> new ChessPiece(color, ChessPiece.PieceType.BISHOP);
            case 4 -> new ChessPiece(color, ChessPiece.PieceType.QUEEN);
            case 5 -> new ChessPiece(color, ChessPiece.PieceType.KING);
            default -> null;
        };
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        emptyBoard();
        setBackRow(ChessGame.TeamColor.BLACK);
        setBackRow(ChessGame.TeamColor.WHITE);
        setPawnRow(ChessGame.TeamColor.BLACK);
        setPawnRow(ChessGame.TeamColor.WHITE);
    }

    /**
     * Empties the board completely
     * (Used to initialize and reset the board)
     */
    public void emptyBoard() {
        Arrays.fill(bitboards, 0L);
    }

    /**
     * Function to set a given team color's row to pawns.
     * @param color is the team color we're resetting.
     */
    private void setPawnRow(ChessGame.TeamColor color) {
        int row = 1;
        int pawns = WHITE_PAWNS;
        if (color == ChessGame.TeamColor.BLACK) {
            pawns = BLACK_PAWNS;
            row = 6;
        }
        int index = row * 8;

        for (int i = index; i < index + 8; i++) {
            bitboards[pawns] |= 1L << i;
        }
    }

    /**
     * Function to set a given team color's back row to game start.
     * @param color is the given team.
     */
    private void setBackRow(ChessGame.TeamColor color) {
        int[] pieces;
        if (color == ChessGame.TeamColor.WHITE) {
            pieces = new int[]{WHITE_ROOKS, WHITE_KNIGHTS, WHITE_BISHOPS, WHITE_QUEENS, WHITE_KINGS,
                    WHITE_BISHOPS, WHITE_KNIGHTS, WHITE_ROOKS};
        }
        else {
            pieces = new int[]{BLACK_ROOKS, BLACK_KNIGHTS, BLACK_BISHOPS, BLACK_QUEENS, BLACK_KINGS,
                    BLACK_BISHOPS, BLACK_KNIGHTS, BLACK_ROOKS};
        }
        int row = (color == ChessGame.TeamColor.WHITE) ? 0 : 7;
        int index = row * 8;

        // For loop with switch statement to place the correct pieces
        for (int i = index; i < index + 8; i++) {
            bitboards[pieces[i % 8]] |= 1L << i;
        }
    }

    /**
     * Method to get enPassant status. Currently not implemented.
     * @return an int index of a pawn that can be used for en passant
     */
    public int getEnPassant() {
        return enPassant;
    }

    /**
     * Method to set the index of possible en passant movement
     * @param index
     */
    public void setEnPassant(int index) {
        enPassant = index;
    }

    /**
     * Getter to get the array of bitboards representing the gameboard
     * @return
     */
    public long[] getBitboards() {
        return bitboards;
    }

    /**
     * Method to make a deep copy of the given board
     * @param board is the board we're copying
     */
    public void copy(ChessBoard board){
        long[] copyBitboards = board.getBitboards();
        System.arraycopy(copyBitboards, 0, bitboards, 0, bitboards.length);
        setEnPassant(board.getEnPassant());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(bitboards, that.bitboards);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bitboards);
    }
}
