package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessPosition;

public class RookMove extends LinearMover{
    RookMove(ChessBoard board, ChessPosition position) {
        super(board, position);
    }
}
