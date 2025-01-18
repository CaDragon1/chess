package chess.pieceMoves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.HashSet;

/** Knights have 8 possible moves.
 * - o - o -
 * o - - - o
 * - - K - -
 * o - - - o
 * - o - o -
 * these positions are, relative to the knight's position, in (column, row) terms:
 * (-1, +2) (+1, +2) (-2, +1) (2, +1) (-2, -1) (2, -1) (-1, -2) (+1, -2)
 * Essentially, if the absolute value of (knightPosition.getRow() - row) + abs(knightPosition.getCol() - col) = 3,
 * then as long as that spot is within the board, it is a possible move.
 */

public class KnightMoves extends PieceMoves {
    public KnightMoves(ChessBoard gameBoard, ChessPosition startPosition) {
        super(gameBoard, startPosition);
        calculateMoves();
    }

    @Override
    protected void calculateMoves() {
        for (int row = startPosition.getRow() - 2; row <= startPosition.getRow() + 2; row++) {
            for (int col = startPosition.getColumn() - 2; col <= startPosition.getColumn() + 2; col++) {
                if (Math.abs(startPosition.getRow() - row) + Math.abs(startPosition.getColumn() - col) == 3
                && row <= 8 && col <= 8 && row > 0 && col > 0) {
                    ChessPosition checkedPosition = new ChessPosition(row, col);
                    checkSpace(checkedPosition, gameBoard.getPiece(checkedPosition));
                }
            }
        }
    }

    @Override
    public HashSet<ChessMove> getMoveList() {
        return moveList;
    }
}
