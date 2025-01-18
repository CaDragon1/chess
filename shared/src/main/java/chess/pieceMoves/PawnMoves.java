package chess.pieceMoves;

import chess.*;

import java.util.HashSet;

public class PawnMoves extends PieceMoves {
    /**
     * Because pawns are directionally variable depending on the team color, we need a variable to store the pawn's color.
     */
    ChessGame.TeamColor pieceColor;
    int direction;
    int endzone;

    public PawnMoves(ChessBoard gameBoard, ChessPosition startPosition) {
        super(gameBoard, startPosition);
        pieceColor = gameBoard.getPiece(startPosition).getTeamColor();
        direction = setDirection();
        endzone = setEndzone();
        calculateMoves();
    }

    @Override
    protected void calculateMoves() {
        checkFront();
        checkDiagonals();
    }

    /**
     * Function to check if it's the piece's starting move. If so, we can calculate that the
     * pawn can move two spaces instead of one.
     * @return true if is starting move, false if not.
     */
    private boolean isStartingMove() {
        if (pieceColor == ChessGame.TeamColor.BLACK && startPosition.getRow() == 7) {
            return true;
        }
        else if (pieceColor == ChessGame.TeamColor.WHITE && startPosition.getRow() == 2) {
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Function to set the direction the pawn will travel in based on team color.
     * @return 1 (travel up) if white, -1 (travel down) if black.
     */
    private int setDirection() {
        if (pieceColor == ChessGame.TeamColor.BLACK) {
            return -1;
        }
        else {
            return 1;
        }
    }

    /**
     * Function to set the end of the board for the pawn, useful for when checking promotability.
     * @return 8 if pieceColor is white, 1 if pieceColor is black.
     */
    private int setEndzone() {
        if (pieceColor == ChessGame.TeamColor.BLACK) {
            return 1;
        }
        else {
            return 8;
        }
    }

    private void checkFront() {
        ChessPosition checkedPosition = new ChessPosition(startPosition.getRow() + direction, startPosition.getColumn());
        if (gameBoard.getPiece(checkedPosition) == null) {
            addPawnMove(checkedPosition);
            if (isStartingMove()) {
                checkedPosition = new ChessPosition(startPosition.getRow() + (2 * direction), startPosition.getColumn());
                if (gameBoard.getPiece(checkedPosition) == null) {
                    addPawnMove(checkedPosition);
                }
            }
        }
    }

    private void checkDiagonals() {
        for (int col = startPosition.getColumn() - 1; col <= startPosition.getColumn() + 1; col+=2) {
            if (col <= 8 && col > 0) {
                ChessPosition checkedPosition = new ChessPosition(startPosition.getRow() + direction, col);
                ChessPiece checkedPiece = gameBoard.getPiece(checkedPosition);
                if (checkedPiece != null) {
                    // Is the piece an enemy? If so, we can attack.
                    if (checkedPiece.getTeamColor() != pieceColor) {
                        addPawnMove(checkedPosition);
                    }
                }
            }
        }

    }

    /**
     * We need a separate function to add pawn moves to the move list, because if the pawn reaches the end, it can
     * be promoted.
     * @param checkedPosition is the position we want to use to add a new move to the moveList
     */
    private void addPawnMove(ChessPosition checkedPosition) {
        if (checkedPosition.getRow() == endzone) {
            for (ChessPiece.PieceType type : ChessPiece.PieceType.values()) {
                moveList.add(new ChessMove(startPosition, checkedPosition, type));
            }
        }
        else {
            moveList.add(new ChessMove(startPosition, checkedPosition));
        }
    }

    @Override
    public HashSet<ChessMove> getMoveList() {
        return moveList;
    }
}