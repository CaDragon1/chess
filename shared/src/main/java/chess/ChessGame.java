package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    TeamColor currentTeam;
    ChessBoard gameBoard;

    public ChessGame() {
        currentTeam = TeamColor.WHITE;
        gameBoard = new ChessBoard();
        gameBoard.resetBoard();
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeam;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeam = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> pieceMoves;
        if (gameBoard.getPiece(startPosition) != null) {
            pieceMoves = gameBoard.getPiece(startPosition).pieceMoves(gameBoard, startPosition);
            pieceMoves.removeIf(this::testMove);
            return pieceMoves;
        }
        else return null;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (gameBoard.getPiece(move.getStartPosition()) == null) {
            throw new InvalidMoveException("no piece at start position");
        }
        else if (!validMoves(move.getStartPosition()).contains(move)) {
            throw new InvalidMoveException("move is invalid");
        }
        else if (gameBoard.getPiece(move.getStartPosition()).getTeamColor() != currentTeam) {
            throw new InvalidMoveException("cannot play out of turn");
        }
        else {
            moveMaker(move);
            currentTeam = (currentTeam == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        }
    }


    /**
     * A helper function so that if I'm guaranteeing the move is valid already, no exceptions need be thrown.
     * This was made because the tests gave me trouble.
     * @param move is the move to make.
     */
    private void moveMaker(ChessMove move) {
        ChessPiece movePiece = gameBoard.getPiece(move.getStartPosition());
        if (move.getPromotionPiece() != null) {
            gameBoard.addPiece(move.getEndPosition(), new ChessPiece(movePiece.getTeamColor(), move.getPromotionPiece()));
        } else {
            gameBoard.addPiece(move.getEndPosition(), movePiece);
        }
        gameBoard.addPiece(move.getStartPosition(), null);
    }

    /**
     * Undo a given move and replace the captured piece.
     * @param move The move we want to undo
     * @param capturedPiece The piece in the location we captured
     */
    public void undoMove(ChessMove move, ChessPiece capturedPiece) {
        if (move.getPromotionPiece() != null) {
            gameBoard.addPiece(move.getStartPosition(),
                    new ChessPiece(gameBoard.getPiece(move.getEndPosition()).getTeamColor(), ChessPiece.PieceType.PAWN));
        }
        else {
            gameBoard.addPiece(move.getStartPosition(), gameBoard.getPiece(move.getEndPosition()));
        }
        gameBoard.addPiece(move.getEndPosition(), capturedPiece);
    }


    /**
     * testMove determines whether a specific move puts the king in check.
     * @param move the move to test
     * @return true if check, false if not
     */
    public boolean testMove(ChessMove move) {
        boolean inCheck;
        ChessPiece targetPiece = gameBoard.getPiece(move.getEndPosition());
        moveMaker(move);
        inCheck = isInCheck(gameBoard.getPiece(move.getEndPosition()).getTeamColor());
        undoMove(move, targetPiece);
        return inCheck;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {

        ChessPosition kingPosition = findKing(teamColor);
        if (kingPosition != null) {
            if (checkKingStraights(kingPosition)) {
                System.out.println("The king has been noted as in check.");
                return true;
            }
            System.out.println("Checking for threatening knights");
            return checkKingKnights(kingPosition);
        }
        return false;
    }


    /**
     * Finds where the king of a given team is
     *
     * @param teamColor which team to find the king of
     * @return the ChessPosition of the king
     */
    private ChessPosition findKing(TeamColor teamColor) {
        ChessPosition checkPosition;
        ChessPiece checkPiece;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                checkPosition = new ChessPosition(i, j);
                checkPiece = gameBoard.getPiece(checkPosition);
                if (checkPiece != null && checkPiece.getTeamColor() == teamColor
                        && checkPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    return checkPosition;
                }
            }
        }
        System.out.println("ERROR: No king of team " + teamColor.toString() + " found.");
        return null;
    }

    private boolean checkKingStraights(ChessPosition kingPosition) {
        for (int rowMod = -1; rowMod <= 1; rowMod++) {
            for (int colMod = -1; colMod <= 1; colMod++) {
                if (rowMod != 0 || colMod != 0) {
                    if (straightChecker(kingPosition, rowMod, colMod)) {
                        System.out.println("The king is in check.");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Method to run through a single 'ray' that originates from the king's position, checking to see if the king
     * is in any danger from that direction.
     * @param kingPosition is the king's position
     * @param rowMod is the direction along the rows that we want to travel (-1 for down, 1 for up)
     * @param colMod is the direction along the columns that we want to travel (-1 for left, 1 for right)
     * @return true if the king is in check from that direction, false if not.
     */
    private boolean straightChecker(ChessPosition kingPosition, int rowMod, int colMod) {
        ChessPosition checkPosition;
        ChessPiece checkPiece;
        ChessGame.TeamColor kingColor = gameBoard.getPiece(kingPosition).getTeamColor();
        int row = kingPosition.getRow() + rowMod;
        int col = kingPosition.getColumn() + colMod;

        while (isInBounds(row, col)) {
            checkPosition = new ChessPosition(row, col);
            if (gameBoard.getPiece(checkPosition) != null){
                checkPiece = gameBoard.getPiece(checkPosition);
                if (checkPiece.getTeamColor() != kingColor) {
                    return targetingKing(kingPosition, checkPiece, checkPosition);
                }
            }
            row += rowMod;
            col += colMod;
        }
        return false;
    }

    /**
     * Method to determine whether a chess piece is targeting the king.
     * @param kingPosition is the king's position
     * @param checkPiece is the piece that may or may not be attacking the king
     * @param checkPosition is the position of the piece that may or may not be attacking the king
     * @return true if the king's position is included in the piece's movelist
     */
    private boolean targetingKing(ChessPosition kingPosition, ChessPiece checkPiece, ChessPosition checkPosition) {
        // Create a movelist for the selected chess piece.
        Collection<ChessMove> targetMoves = checkPiece.pieceMoves(gameBoard, checkPosition);
        System.out.println("\n" + targetMoves.toString());

        // Create two example moves that target the king's position. One for generic pieces,
        // one for pawn promotion moves.
        ChessMove dangerMove = new ChessMove(checkPosition, kingPosition);
        ChessMove pawnDangerMove = new ChessMove(checkPosition, kingPosition, ChessPiece.PieceType.QUEEN);

        // If the danger move is in the move list, the king is in check.
        System.out.println("Checking king position in enemy movelist");
        System.out.println(dangerMove + "\n");
        return targetMoves.contains(dangerMove) || targetMoves.contains(pawnDangerMove);
    }

    private boolean checkKingKnights(ChessPosition kingPosition) {
        int kingRow = kingPosition.getRow();
        int kingCol = kingPosition.getColumn();
        TeamColor kingColor = gameBoard.getPiece(kingPosition).getTeamColor();
        ChessPosition checkPosition;

        for (int row = kingRow - 2; row <= kingRow + 2; row ++) {
            for (int col = kingCol - 2; col <= kingCol + 2; col++) {
                if (Math.abs(kingRow - row) + Math.abs(kingCol - col) == 3 && isInBounds(row, col)) {
                    checkPosition = new ChessPosition(row, col);
                    ChessPiece checkPiece = gameBoard.getPiece(checkPosition);
                    if (checkPiece != null && checkPiece.getPieceType() == ChessPiece.PieceType.KNIGHT
                            && checkPiece.getTeamColor() != kingColor) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isInBounds(int row, int col) {
        return row <= 8 && col <= 8 && row >0 && col > 0;
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor){
        if (!isInCheck(teamColor)) {
            return false;
        }
        else {
            for (ChessMove move : getAllTeamMoves(teamColor)) {
                if (!testMove(move)) {
                    return false;
                }
            }

            return true;
        }
    }

    private Collection<ChessMove> getAllTeamMoves(TeamColor teamColor) {
        Collection<ChessMove> allMoves = new HashSet<>();
        ChessPosition checkPosition;
        ChessPiece checkPiece;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                checkPosition = new ChessPosition(row, col);
                checkPiece = gameBoard.getPiece(checkPosition);
                if (checkPiece != null && checkPiece.getTeamColor() == teamColor) {
                    allMoves.addAll(checkPiece.pieceMoves(gameBoard, checkPosition));
                }
            }
        }
        return allMoves;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        ChessPosition setPosition;
        ChessPiece setPiece;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                setPosition = new ChessPosition(i, j);
                if (board.getPiece(setPosition) != null) {
                    setPiece = new ChessPiece(board.getPiece(setPosition).getTeamColor(), board.getPiece(setPosition).getPieceType());
                    gameBoard.addPiece(setPosition, setPiece);
                }
                else {
                    gameBoard.addPiece(setPosition, null);
                }
            }
        }
        gameBoard.printBoard();
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessGame chessGame)) {
            return false;
        }
        return currentTeam == chessGame.currentTeam && Objects.equals(gameBoard, chessGame.gameBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTeam, gameBoard);
    }
}
