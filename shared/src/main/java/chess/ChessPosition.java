package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int row;
    private final int col;

    /**
     * Basic constructor based on row and column
     * @param row is row (1-8)
     * @param col is col (1-8)
     */
    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * Method to return string-formatted chess coordinates
     * @return the coordinates in letter-number format
     */
    public String getCoordinates() {
        char file = (char) ('a' + (col - 1));
        return "" + file + row;
    }

    /**
     * Basic constructor based on a bitboard index
     * @param index is an int between 0 and 63
     */
    public ChessPosition(int index) {
        this.row = (index / 8) + 1;
        this.col = (index % 8) + 1;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return col;
    }

    /**
     * @return the bitboard index of the position, shifting left by 1 to fit 0-63 indexing.
     */
    public int getIndex() {
        return (row - 1) * 8 + (col - 1);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && col == that.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
