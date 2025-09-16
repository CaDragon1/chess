package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.List;

/**
 * LinearMover is a class that extends MoveCalculator, adding in a linear movement checker.
 */

public class LinearMover extends MoveCalculator{
    LinearMover(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    /**
     * calculateLinear will be called as part of moveCalculator.
     * @param horizontalDirection will go left if -1, right if 1, and stay still if 0.
     * @param verticalDirection will go down if -1, up if 1, and stay still if 0.
     */
    private void calculateLinear(int horizontalDirection, int verticalDirection) {
        int index = position.getIndex();
        int checking = index;

        ChessPiece startingPiece = board.getPiece(index);
        ChessPiece foundPiece;
        boolean obstructed = false;

        while (!obstructed) {
            checking = checking + horizontalDirection + (8 * verticalDirection);
            /** Check for board bounds **/
            obstructed = checkBoundaries(index, checking);
            foundPiece = board.getPiece(checking);
            if (foundPiece == null) {
                moveList.add(new ChessMove(index, checking, null));
            }
            else if (foundPiece.getTeamColor() != startingPiece.getTeamColor()) {
                moveList.add(new ChessMove(index, checking, null));
                obstructed = true;
            }
            else {
                obstructed = true;
            }
        }
    }
}
