package chess.movecalculator;

import chess.ChessMove;

import java.util.List;

/**
 * LinearMover is a class that extends MoveCalculator, adding in a linear movement checker.
 */

public class LinearMover extends MoveCalculator{
    /**
     * calculateLinear will be called as part of moveCalculator.
     * @param horizontalDirection will go left if -1, right if 1, and stay still if 0.
     * @param verticalDirection will go down if -1, up if 1, and stay still if 0.
     */
    private void calculateLinear(int horizontalDirection, int verticalDirection) {throw new RuntimeException("Not implemented");}
}
