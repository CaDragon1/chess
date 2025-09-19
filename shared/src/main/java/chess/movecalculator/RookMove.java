package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessPosition;

public class RookMove extends LinearMover{
    public RookMove(ChessBoard board, ChessPosition position) {
        super(board, position);
        calculateMoves();
    }

    private void calculateMoves() {
        for (int horizontal = -1; horizontal <= 1; horizontal+=2) {
            calculateLinear(horizontal, 0);
        }
        for (int vertical = -1; vertical <= 1; vertical+=2) {
            calculateLinear(0, vertical);
        }
    }
}
