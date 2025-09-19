package chess.movecalculator;

import chess.*;

public class KingMove extends MoveCalculator{
    public KingMove(ChessBoard board, ChessPosition position) {
        super(board, position);
        calculateMoves();
    }

    private void calculateMoves() {
        int index = position.getIndex();
        int endIndex;
        ChessGame.TeamColor kingColor = board.getPiece(index).getTeamColor();

        for (int horizontal = -1; horizontal <= 1; horizontal++) {
            for (int vertical = -1; vertical <= 1; vertical++) {
                if (horizontal != 0 || vertical != 0) {
                    endIndex = index + horizontal + (8 * vertical);
                    addMoveIfValid(index, endIndex, kingColor);
                }
            }
        }
    }
    private void addMoveIfValid(int startIndex, int endIndex, ChessGame.TeamColor kingColor) {
        ChessPiece targetPiece = board.getPiece(endIndex);
        if (!isOutOfBounds(startIndex, endIndex)) {
            if (targetPiece == null || targetPiece.getTeamColor() != kingColor){
                moveList.add(new ChessMove(startIndex, endIndex, null));
            }
        }
    }
}
