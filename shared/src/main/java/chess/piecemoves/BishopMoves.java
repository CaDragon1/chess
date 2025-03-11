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

//    /**
//     * A function designed to check the diagonal spaces spanning from startPosition.
//     * @param right is the amount of columns we shift to the right each iteration (1 or -1)
//     * @param up is the amount of columns we shift upwards each iteration (1 or -1)
//     */
//    private void checkDiagonal(int right, int up) {
//        int horizontal = startPosition.getColumn() + right;
//        int vertical = startPosition.getRow() + up;
//
//        boolean keepChecking = true;
//        while (keepChecking) {
//            // Make sure the checked area is within bounds
//            if (horizontal <= 8 && horizontal > 0 && vertical <= 8 && vertical > 0) {
//                ChessPosition checkedPosition = new ChessPosition(vertical, horizontal);
//                ChessPiece checkedPiece = gameBoard.getPiece(checkedPosition);
//
//                // Call checkSpace to add our space to the movelist or determine we must stop.
//                keepChecking = checkSpace(checkedPosition, checkedPiece);
//
//                // Increment horizontal and vertical
//                horizontal += right;
//                vertical += up;
//            }
//            // If the index is out of bounds, we cannot move there.
//            else {
//                keepChecking = false;
//            }
//        }
//    }

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
