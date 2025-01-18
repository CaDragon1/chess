package chess.pieceMoves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public class RookMoves extends PieceMoves {
    public RookMoves(ChessBoard gameBoard, ChessPosition startPosition) {
        super(gameBoard, startPosition);
        calculateMoves();
    }

    @Override
    protected void calculateMoves() {
        // Check all four directions
        checkStraight(-1, 0);
        checkStraight(1, 0);
        checkStraight(0, -1);
        checkStraight(0, 1);
    }

    /**
     * A function designed to check the vertical spaces spanning from startPosition.
     * @param up is the amount of columns we shift upwards each iteration (1 or -1)
     */
    private void checkVertical(int up) {
        int vertical = startPosition.getRow() + up;

        boolean keepChecking = true;
        while (keepChecking) {
            // Make sure the checked area is within bounds
            if (vertical <= 8 && vertical > 0) {
                ChessPosition checkedPosition = new ChessPosition(vertical, startPosition.getColumn());
                ChessPiece checkedPiece = gameBoard.getPiece(checkedPosition);

                // Call keepChecking to add our space to the movelist or to see if we need to stop.
                keepChecking = checkSpace(checkedPosition, checkedPiece);

                // Increment vertical
                vertical += up;
            }
            // If the index is out of bounds, we can't move there.
            else {
                keepChecking = false;
            }
        }
    }

    /**
     * A function designed to check the horizontal spaces spanning from startPosition.
     * @param right is the amount of columns we shift to the right each iteration (1 or -1)
     */
    private void checkHorizontal(int right) {
        int horizontal = startPosition.getColumn() + right;

        boolean keepChecking = true;
        while (keepChecking) {
            // Make sure the checked area is within bounds
            if (horizontal <= 8 && horizontal > 0) {
                ChessPosition checkedPosition = new ChessPosition(startPosition.getRow(), horizontal);
                ChessPiece checkedPiece = gameBoard.getPiece(checkedPosition);

                // Call checkSpace to add our space to the movelist or to see if we need to stop.
                keepChecking = checkSpace(checkedPosition, checkedPiece);

                // Increment vertical
                horizontal += right;
            }
            // If the index is out of bounds, we can't move there.
            else {
                keepChecking = false;
            }
        }
    }

    /**
     * Calculates all possible moves along a straight ray originating from the starting point.
     * Depending on the parameters given, this can be applied horizontally, vertically, and diagonally in all directions.
     * @param right Possible values to be given: -1 (checks left), 0 (stays in same column), 1 (checks right)
     * @param up Possible values to be given: -1 (checks down), 0 (stays in same row), 1 (checks up)
     */
    private void checkStraight(int right, int up) {

        // If only up has a nonzero value, we check vertically.
        if (up != 0) {
            checkVertical(up);
        }
        // Otherwise, we check horizontally.
        else if (right != 0) {
            checkHorizontal(right);
        }
        else {
            System.out.println("checkStraight bounds exceeded");
        }

    }

    @Override
    public HashSet<ChessMove> getMoveList() {
        return moveList;
    }
}
