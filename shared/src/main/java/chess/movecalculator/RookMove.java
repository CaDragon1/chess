package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessPosition;

public class RookMove extends LinearMover{
    public RookMove(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    private void calculateMoves() {
        for (int horizontal = -1; horizontal <= 1; horizontal++) {
            calculateLinear(horizontal, 0);
        }
        for (int vertical = -1; vertical <= 1; vertical++) {
            calculateLinear(0, vertical);
        }
    }
}
