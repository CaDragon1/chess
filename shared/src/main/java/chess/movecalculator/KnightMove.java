package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

public class KnightMove extends MoveCalculator{
    public KnightMove(ChessBoard board, ChessPosition position) {
        super(board, position);
    }

    private void calculateMoves() {
        ChessGame.TeamColor starterColor = board.getPiece(position).getTeamColor();
        int startIndex = position.getIndex();
        int checkIndex;
        for (int horizontal = -2; horizontal <= 2; horizontal++) {
            for (int vertical = -2; vertical <= 2; vertical++) {
                if (Math.abs(horizontal) + Math.abs(vertical) == 3) {
                checkIndex = startIndex + horizontal + (8 * vertical);
                    if (!isOutOfBounds(startIndex, checkIndex)) {
                        checkPosition(startIndex, checkIndex, starterColor);
                    }
                }
            }
        }
    }

    private void checkPosition(int index, int checking, ChessGame.TeamColor teamColor) {
        chess.ChessPiece foundPiece = board.getPiece(checking);
        if (foundPiece == null) {
            moveList.add(new ChessMove(index, checking, null));
        }
        else if (foundPiece.getTeamColor() != teamColor) {
            moveList.add(new ChessMove(index, checking, null));
        }
    }
}
