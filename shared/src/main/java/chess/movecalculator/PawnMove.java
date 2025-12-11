package chess.movecalculator;

import chess.*;

public class PawnMove extends MoveCalculator{
    private final int vertical;
    private final int startingRow;
    ChessGame.TeamColor currentTeam;

    public PawnMove(ChessBoard board, ChessPosition position) {

        super(board, position);
        currentTeam = board.getPiece(position).getTeamColor();
        vertical = currentTeam == ChessGame.TeamColor.WHITE ? 1 : -1;
        startingRow = currentTeam == ChessGame.TeamColor.WHITE ? 2 : 7;
        calculateMoves();
    }

    private void calculateMoves() {
        // Check if en passant is possible and if the current row is 4 or 5, depending on team color:
        int checkPosition = position.getIndex() + (vertical * 8);
        boolean obstructed = checkMove(checkPosition);

        // Check for attacking spaces
        checkAttack();

        // If those aren't met, check to see if we're in the starting row and check for a double move
        if (position.getRow() == startingRow && !obstructed) {
            checkPosition+=(8 * vertical);
            checkMove(checkPosition);
        }
    }

    /**
     * checkMove determines standard forward movement for the pawn.
     * @param checking is the index we are checking for move validity.
     * @return "true" if the move is obstructed; otherwise, adds the move.
     */
    private boolean checkMove(int checking) {
        int startingIndex = position.getIndex();
        if (isOutOfBounds(startingIndex, checking) || board.getPiece(checking) != null) {
            return true;
        }
        if (isPromotable(checking)) {
            addPromoMoves(checking);
        }
        else {
            moveList.add(new ChessMove(startingIndex, checking, null));
        }
        return false;
    }

    /**
     * checkAttack is our attacking function; it checks both diagonals, then determines if the pawn can move there.
     */
    private void checkAttack() {
        int checkPosition = position.getIndex() + (8 * vertical);
        for (int i = -1; i <= 1; i+=2) {
            // Check in bounds
            if (!isOutOfBounds(position.getIndex(), checkPosition + i)) {
                ChessPiece occupyingPiece = board.getPiece(checkPosition + i);
                // Make sure there's an enemy piece there
                isAttackingEnemy(occupyingPiece, checkPosition + i);
            }
        }
    }

    /**
     * isAttackingEnemy determines if an enemy is in the targeted space, then adds the move if so.
     * @param occupyingPiece is the piece currently in the targeted space
     * @param checkPosition is the position we're trying to check
     */
    private void isAttackingEnemy(ChessPiece occupyingPiece, int checkPosition) {
        if (occupyingPiece != null && occupyingPiece.getTeamColor() != currentTeam) {
            if (isPromotable(checkPosition)) {
                addPromoMoves(checkPosition);
            }
            else {
                moveList.add(new ChessMove(position.getIndex(), checkPosition, null));
            }
        }
    }

    /**
     * Determines whether or not the index leads to the pawn being promoted
     * @param index is the checked index
     * @return true if the space is a promotable space
     */
    private boolean isPromotable(int index) {
        if (currentTeam == ChessGame.TeamColor.WHITE) {
            return index / 8 == 7;
        }
        else {
            return index / 8 == 0;
        }
    }

    /**
     * addPromoMoves adds all promotion pieces as new chessmoves.
     * @param index is the index of the space the pawn is moving to
     */
    private void addPromoMoves(int index) {
        moveList.add(new ChessMove(position.getIndex(), index, ChessPiece.PieceType.ROOK));
        moveList.add(new ChessMove(position.getIndex(), index, ChessPiece.PieceType.KNIGHT));
        moveList.add(new ChessMove(position.getIndex(), index, ChessPiece.PieceType.BISHOP));
        moveList.add(new ChessMove(position.getIndex(), index, ChessPiece.PieceType.QUEEN));
    }
}