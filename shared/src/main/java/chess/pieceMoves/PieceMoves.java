package chess.pieceMoves;

import chess.ChessMove;
import chess.ChessBoard;
import chess.ChessPosition;

import java.util.HashSet;

public abstract class PieceMoves {
    /**
     * gameBoard is the current boardstate, used to determine what moves are possible.
     * moveList is a Collection<> of ChessMove objects.
     */
    protected ChessBoard gameBoard;
    protected HashSet<ChessMove> moveList;

    PieceMoves(ChessBoard gameBoard, ChessPosition startPosition){
        this.gameBoard = gameBoard;
    }

    protected abstract void calculateMoves();
    public abstract HashSet<ChessMove> getMoveList();


}
