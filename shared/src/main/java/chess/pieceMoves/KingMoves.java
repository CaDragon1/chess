package chess.pieceMoves;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.HashSet;

public class KingMoves extends PieceMoves {
    public KingMoves(ChessBoard gameBoard, ChessPosition startPosition) {
        super(gameBoard, startPosition);
        calculateMoves();
    }

    @Override
    protected void calculateMoves() {
        for (int i = -1; i <= 1; i++) {
            int row = startPosition.getRow() + i;
            for (int j = -1; j <= 1; j++) {
                int col = startPosition.getColumn() + j;
                if(row <= 8 && col <= 8 && row > 0 && col > 0) {
                    ChessPosition checkedPosition = new ChessPosition(row, col);
                    checkSpace(checkedPosition, gameBoard.getPiece(checkedPosition));
                }
            }
        }
    }

    @Override
    public HashSet<ChessMove> getMoveList() {
        return moveList;
    }
}
