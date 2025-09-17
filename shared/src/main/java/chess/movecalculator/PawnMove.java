package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessPosition;

public class PawnMove extends MoveCalculator{
    PawnMove(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    private void calculateMoves() {throw new RuntimeException("Not implemented");}

}
