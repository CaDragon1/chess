package chess;

import java.util.Arrays;

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


    public ChessBoard() {
        emptyBoard();
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Empties the board completely
     * (Used to initialize and reset the board)
     */
    public void emptyBoard() {
        Arrays.fill(bitboards, 0L);
    }

    public void setPawnRow(ChessGame.TeamColor color) {
        int row = 1;
        int pawns = WHITE_PAWNS;
        if (color == ChessGame.TeamColor.BLACK) {
            pawns = BLACK_PAWNS;
            row = 6;
        }
        int index = row * 8;

        for (int i = index; i < index + 8; i++){
            bitboards[pawns] |= 1L << i;
        }
    }
}
