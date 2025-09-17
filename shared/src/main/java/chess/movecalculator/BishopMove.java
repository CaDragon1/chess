package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessPosition;

public class BishopMove extends LinearMover{
    BishopMove(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    private void calculateMoves() {
        for (int horizontal = -1; horizontal <= 1; horizontal+=2) {
            for (int vertical = -1; vertical <= 1; vertical+=2) {
                calculateLinear(horizontal, vertical);
            }
        }
    }
}
