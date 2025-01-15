package chess.pieceMoves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.HashSet;

public class KingMoves extends PieceMoves {
    KingMoves(ChessBoard gameBoard, ChessPosition startPosition) {
        super(gameBoard, startPosition);
    }

    @Override
    protected void calculateMoves() {

    }

    @Override
    public HashSet<ChessMove> getMoveList() {
        return null;
    }
}
