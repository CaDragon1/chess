package chess.movecalculator;

import chess.*;

public class PawnMove extends MoveCalculator{
    private final int vertical;
    private final int epRow;
    ChessGame.TeamColor currentTeam;

    PawnMove(ChessBoard board, ChessPosition position) {

        super(board, position);
        currentTeam = board.getPiece(position).getTeamColor();
        vertical = currentTeam == ChessGame.TeamColor.WHITE ? 1 : -1;
        epRow = currentTeam == ChessGame.TeamColor.WHITE ? 5 : 4;
    }

    private void calculateMoves() {
        // Check if en passant is possible and if the current row is 4 or 5, depending on team color:
        if (board.getEnPassant() != -1 && position.getRow() == epRow) {
            checkEnPassant();
        }
    }

    private void checkAttack() {
        int checkPosition = position.getIndex() + (8 * vertical);
        for (int i = -1; i <= 1; i+=2) {
            // Check in bounds
            if (isOutOfBounds(checkPosition + i)) {
                ChessPiece occupyingPiece = board.getPiece(checkPosition + i);
                // Make sure there's an enemy piece there
                if (occupyingPiece != null && occupyingPiece.getTeamColor() != currentTeam) {
                    moveList.add(new ChessMove(position.getIndex(), checkPosition + i, null));
                }
            }
        }
    }

    /**
     * The way En Passant works:
     * - ChessBoard stores the index of the pawn that is vulnerable to en passant.
     * - If the PawnMove detects that index next to the index of the current position, it will add that diagonal to the move list.
     * - When a move is made later, if the move's final position matches the en passant index, then the relevant pawn
     * is also removed if the currently moving piece is also a pawn.
     * - Regardless, after any move is made, enPassant is set to -1; if the move being made is a pawn +2,
     * we set en passant to the new position's index.
     */
    private void checkEnPassant() {
        int epIndex = board.getEnPassant();

        if (position.getIndex() + 1 == epIndex || position.getIndex() - 1 == epIndex) {
            moveList.add(new ChessMove(position.getIndex(), epIndex + (8 * vertical), null));
        }
    }

    private boolean isPromotable(int index) {
        if (currentTeam == ChessGame.TeamColor.WHITE) {
            return index / 8 == 7;
        }
        else return index / 8 == 0;
    }

    private void addPromoMoves(int index) {

    }

}
