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
    private final ChessPiece[][] gameBoard;

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
        for (int i = 1; i <= 8; i++){
            switch (i){
                case 1:
                    setBackRow(i, ChessGame.TeamColor.WHITE);
                    break;
                case 2:
                    setPawnRow(i, ChessGame.TeamColor.WHITE);
                    break;
                case 7:
                    setPawnRow(i, ChessGame.TeamColor.BLACK);
                    break;
                case 8:
                    setBackRow(i, ChessGame.TeamColor.BLACK);
                    break;
                default:
                    setNullSpaces(i);
            }
        }
    }

    /**
     * Sets the given row to a line of pawns of the given color
     * @param row The row to place the pawns on
     * @param teamColor The team color to make the placed pawns
     */
    private void setPawnRow(int row, ChessGame.TeamColor teamColor) {
        for (int col = 1; col <= 8; col++){
            addPiece(new ChessPosition(row, col), new ChessPiece(teamColor, ChessPiece.PieceType.PAWN));
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
        for (int i = 1; i <= 8; i++) {
            switch (i){
                case 1, 8:
                    addPiece(new ChessPosition(row, i), new ChessPiece(teamColor, ChessPiece.PieceType.ROOK));
                    break;
                case 2, 7:
                    addPiece(new ChessPosition(row, i), new ChessPiece(teamColor, ChessPiece.PieceType.KNIGHT));
                    break;
                case 3, 6:
                    addPiece(new ChessPosition(row, i), new ChessPiece(teamColor, ChessPiece.PieceType.BISHOP));
                    break;
                case 4:
                    addPiece(new ChessPosition(row, i), new ChessPiece(teamColor, ChessPiece.PieceType.QUEEN));
                    break;
                case 5:
                    addPiece(new ChessPosition(row, i), new ChessPiece(teamColor, ChessPiece.PieceType.KING));
                    break;
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
        }
    }

    /**
     * This is a helper function to print the current chess board to the console.
     */
    public void printBoard() {
        for (int temprow = 0; temprow < 8; temprow++){
            for (int tempcol = 0; tempcol < 8; tempcol++) {
                if (gameBoard[temprow][tempcol] != null) {
                    ChessPiece selectedPiece = gameBoard[temprow][tempcol];
                    printPiece(selectedPiece);
                }
                else {
                    System.out.print("[   ]");
                }
            }
            System.out.println();
        }
    }

    /**
     * This is a helper function to help printBoard() print the chess pieces.
     * @param selectedPiece is the current piece selected by printBoard().
     */
    private void printPiece(ChessPiece selectedPiece){
        if (selectedPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            System.out.print("[B");
        }
        else {
            System.out.print("[W");
        }
        if (selectedPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
            System.out.print(" P]");
        }
        else if (selectedPiece.getPieceType() == ChessPiece.PieceType.ROOK) {
            System.out.print(" R]");
        }
        else if (selectedPiece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
            System.out.print("Kn]");
        }
        else if (selectedPiece.getPieceType() == ChessPiece.PieceType.BISHOP) {
            System.out.print(" B]");
        }
        else if (selectedPiece.getPieceType() == ChessPiece.PieceType.QUEEN) {
            System.out.print(" Q]");
        }
        else if (selectedPiece.getPieceType() == ChessPiece.PieceType.KING) {
            System.out.print(" K]");
        }
        else {
            System.out.print("  ]");
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
