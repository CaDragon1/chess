package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessPosition;

public class KingMove extends MoveCalculator{
    KingMove(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
    private void calculateMoves() {
        int index = position.getIndex();
        for (int horizontal = -1; horizontal <= 1; horizontal++) {
            for (int vertical = -1; vertical <= 1; vertical++) {
                if (horizontal != 0 || vertical != 0) {

                }
            }
        }
    }
}
