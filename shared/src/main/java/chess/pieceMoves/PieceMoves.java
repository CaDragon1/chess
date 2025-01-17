package chess.pieceMoves;

import chess.ChessMove;
import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.HashSet;

public abstract class PieceMoves {
    /**
     * gameBoard is the current boardstate, used to determine what moves are possible.
     * moveList is a Collection<> of ChessMove objects.
     */
    protected ChessBoard gameBoard;
    protected ChessPosition startPosition;
    protected HashSet<ChessMove> moveList;

    PieceMoves(ChessBoard gameBoard, ChessPosition startPosition){
        this.gameBoard = gameBoard;
        this.startPosition = startPosition;
        moveList = new HashSet<ChessMove>();
    }

    protected abstract void calculateMoves();
    public abstract HashSet<ChessMove> getMoveList();

    /**
     * A function to check if it is possible to move to a specified position.
     * @param checkedPosition is the position that we are checking movability for.
     * @param checkedPiece is the piece (or null object) currently occupying checkedPosition.
     * @return true if we need to check the next position and false if the space was occupied. This return variable
     * is useful for our continually-moving pieces (rook, bishop, queen).
     */
    protected boolean checkSpace(ChessPosition checkedPosition, ChessPiece checkedPiece) {
        // Check to see if there's a piece in the new spot
        if (checkedPiece != null) {
            // Is the piece one of ours? if so, we can't move there.
            if (checkedPiece.getTeamColor() == gameBoard.getPiece(startPosition).getTeamColor()) {
                return false;
            }
            // If not, we CAN move there, but we can't move beyond, so we stop checking.
            else {
                moveList.add(new ChessMove(startPosition, checkedPosition));
                return false;
            }
        }
        // If the checked spot contains a null object, then that's an open space we can move to.
        else {
            moveList.add(new ChessMove(startPosition, checkedPosition));
            return true;
        }
    }
}
