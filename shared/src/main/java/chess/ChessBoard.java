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
    private ChessPiece[][] gameBoard;

    public ChessBoard() {
        gameBoard = new ChessPiece[8][8];
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     *
     * I made sure to subtract 1 from row and column values because Java arrays start from 0
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        gameBoard[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return gameBoard[position.getRow() - 1][position.getColumn() - 1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int i = 0; i < 8; i++){
            switch (i + 1){
                case 1:
                    setBackRow(i, ChessGame.TeamColor.WHITE);
                    System.out.print(" white\n");
                    break;
                case 2:
                    setPawnRow(i, ChessGame.TeamColor.WHITE);
                    System.out.print(" white\n");
                    break;
                case 7:
                    setPawnRow(i, ChessGame.TeamColor.BLACK);
                    System.out.print(" black\n");
                    break;
                case 8:
                    setBackRow(i, ChessGame.TeamColor.BLACK);
                    System.out.print(" black\n");
                    break;
                default:
                    setNullSpaces(i);
                    System.out.println();
            }
        }
    }

    /**
     * Sets the given row to a line of pawns of the given color
     * @param row The row to place the pawns on
     * @param teamColor The team color to make the placed pawns
     */
    private void setPawnRow(int row, ChessGame.TeamColor teamColor) {
        for (int col = 0; col < 8; col++){
            gameBoard[row][col] = new ChessPiece(teamColor, ChessPiece.PieceType.PAWN);
            System.out.print("[P ]");
        }
    }

    /**
     * Sets the given row to a chess game's back row lineup.
     * (Rook, knight, bishop, queen, king, bishop, knight, rook)
     *
     * @param row The row of which to place a chess game's starting back line on
     * @param teamColor The color of which to make the pieces being placed
     */
    private void setBackRow(int row, ChessGame.TeamColor teamColor) {
        for (int i = 0; i < 8; i++) {
            switch (i + 1){
                case 1, 8:
                    gameBoard[row][i] = new ChessPiece(teamColor, ChessPiece.PieceType.ROOK);
                    System.out.print("[R ]");
                    break;
                case 2, 7:
                    gameBoard[row][i] = new ChessPiece(teamColor, ChessPiece.PieceType.KNIGHT);
                    System.out.print("[Kn]");
                    break;
                case 3, 6:
                    gameBoard[row][i] = new ChessPiece(teamColor, ChessPiece.PieceType.BISHOP);
                    System.out.print("[B ]");
                    break;
                case 4:
                    gameBoard[row][i] = new ChessPiece(teamColor, ChessPiece.PieceType.QUEEN);
                    System.out.print("[Q ]");
                    break;
                case 5:
                    gameBoard[row][i] = new ChessPiece(teamColor, ChessPiece.PieceType.KING);
                    System.out.print("[K ]");
                    break;
                default:
                    System.out.print("Invalid board index");
            }
        }
    }

    /**
     * Sets every space in a given row to "null", effectively removing all chess pieces
     *
     * @param row the row to remove all pieces on
     */
    public void setNullSpaces(int row) {
        for (int i = 0; i < 8; i++){
            gameBoard[row][i] = null;
            System.out.print("[  ]");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessBoard that)) {
            return false;
        }
        return Objects.deepEquals(gameBoard, that.gameBoard);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(gameBoard);
    }
}
