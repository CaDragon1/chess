package chess.piecemoves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public class BishopMoves extends PieceMovesFar {
    public BishopMoves(ChessBoard gameBoard, ChessPosition startPosition) {
        super(gameBoard, startPosition);
        calculateMoves();
    }

    @Override
    protected void calculateMoves() {
        checkLine(-1, -1);
        checkLine(1, -1);
        checkLine(-1, 1);
        checkLine(1, 1);
    }

    @Override
    public HashSet<ChessMove> getMoveList() {
        return moveList;
    }
}
