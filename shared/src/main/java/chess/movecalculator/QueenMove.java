package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessPosition;

public class QueenMove extends LinearMover{
    public QueenMove(ChessBoard board, ChessPosition position) {
        super(board, position);
        calculateMoves();
    }

    private void calculateMoves() {
        for (int horizontal = -1; horizontal <= 1; horizontal++) {
            for (int vertical = -1; vertical <= 1; vertical++) {
                calculateLinear(horizontal, vertical);
            }
        }
    }
}
