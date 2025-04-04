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
    ChessMove previousMove;
    CastlingHistory castlingHistory;
    CheckStalemate checkStalemate;
    InCheckDeterminer inCheckDeterminer;

    public ChessGame() {
        currentTeam = TeamColor.WHITE;
        gameBoard = new ChessBoard();
        gameBoard.resetBoard();
        previousMove = null;
        castlingHistory = new CastlingHistory();
        checkStalemate = new CheckStalemate(gameBoard);
        inCheckDeterminer = new InCheckDeterminer(gameBoard);
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
            if (previousMove != null) {

                // Check for en passant
                if (enPassantCheck(startPosition)) {
                    ChessPosition endPosition = new ChessPosition(
                            previousMove.getEndPosition().getRow() + playDirection(),
                            previousMove.getEndPosition().getColumn());
                    pieceMoves.add(new ChessMove(startPosition, endPosition));
                }
            }
            // Check for castling
            ChessPiece checkPiece = gameBoard.getPiece(startPosition);
            if (checkPiece.getPieceType() == ChessPiece.PieceType.KING
                    && startPosition.getColumn() == 5
                    && !isInCheck(gameBoard.getPiece(startPosition).getTeamColor())) {
                if ((checkPiece.getTeamColor() == TeamColor.WHITE && !castlingHistory.isWhiteKingMoved())
                        ||(checkPiece.getTeamColor() == TeamColor.BLACK && !castlingHistory.isBlackKingMoved())) {
                    pieceMoves.addAll(getCastleMoves(startPosition));
                }
            }
            return pieceMoves;
        }
        else {
            return null;
        }
    }

    /**
     * A function to return all possible castle moves at the given position
     * @param startPosition is the position of the king.
     * @return a collection of all possible moves the King can make when castling.
     */
    private Collection<ChessMove> getCastleMoves (ChessPosition startPosition) {
        Collection<ChessMove> castleMoves = new HashSet<>();
        ChessPiece king = gameBoard.getPiece(startPosition);
        
        if (king.getTeamColor() == TeamColor.WHITE && !castlingHistory.isWhiteKingMoved()) {
            if (!castlingHistory.isWhiteKingsideRookMoved() && checkPathClear(startPosition, 1)) {
                castleMoves.add(new ChessMove(startPosition, new ChessPosition(1, 7)));
            }
            if (!castlingHistory.isWhiteQueensideRookMoved() && checkPathClear(startPosition, -1)) {
                castleMoves.add(new ChessMove(startPosition, new ChessPosition(1, 3)));
            }
        }
        else if (king.getTeamColor() == TeamColor.BLACK && !castlingHistory.isBlackKingMoved()) {
            if (!castlingHistory.isBlackKingsideRookMoved() && checkPathClear(startPosition, 1)) {
                castleMoves.add(new ChessMove(startPosition, new ChessPosition(8, 7)));
            }
            if (!castlingHistory.isBlackQueensideRookMoved() && checkPathClear(startPosition, -1)) {
                castleMoves.add(new ChessMove(startPosition, new ChessPosition(8, 3)));
            }
        }
        return castleMoves;
    }

    /**
     * Method to check if the path is clear to castle along for the king.
     * @param startPosition the position of the king
     * @param direction The direction we are looking to castle (-1 for kingside, 1 for queenside)
     * @return true if the coast is clear, false otherwise
     */
    private boolean checkPathClear(ChessPosition startPosition, int direction) {
        ChessPosition pathPosition;
        ChessMove pathMove;
        for (int i = 1; i * direction + 5 > 1 && i * direction + 5 < 8; i++) {
            pathPosition = new ChessPosition(startPosition.getRow(), (i * direction) + 5);
            pathMove = new ChessMove(startPosition, pathPosition);
            if (i <= 2) {
                if (gameBoard.getPiece(pathPosition) != null || testMove(pathMove)) {
                    return false;
                }
            } else if (gameBoard.getPiece(pathPosition) != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks to see if the game will allow for an en passant.
     * @param startPosition is the position our chess piece is at. It may or may not be a pawn.
     * @return false if there is no possible en passant open
     */
    private boolean enPassantCheck (ChessPosition startPosition) {
        if (previousMove != null) {
            ChessPiece checkPiece = gameBoard.getPiece(previousMove.getEndPosition());
            int previousMoveRow = previousMove.getEndPosition().getRow();
            int previousMoveCol = previousMove.getEndPosition().getColumn();

            // Our piece must be a pawn. The previous move must have been a pawn.
            // The positions must align, and the previous pawn must have made a double move off their starting rank.
            if (checkPiece != null && checkPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                return previousMove.getStartPosition().getRow() == (previousMoveRow + (2 * playDirection()))
                        && gameBoard.getPiece(startPosition).getPieceType() == ChessPiece.PieceType.PAWN
                        && startPosition.getRow() == previousMoveRow
                        && (startPosition.getColumn() == previousMoveCol + 1
                        || startPosition.getColumn() == previousMoveCol - 1);
            }
        }
        return false;
    }

    /**
     * Determines the direction of play.
     * @return 1 if white, -1 if black
     */
    private int playDirection() {
        if (currentTeam == TeamColor.WHITE) {
            return 1;
        }
        return -1;
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
            throw new InvalidMoveException("ERROR FOUND WHEN ATTEMPTING MOVE " + move + " {(" +
                    move.getStartPosition().getRow() + ", " + move.getStartPosition().getColumn() + ") ("
                    + move.getEndPosition().getRow() + ", " + move.getEndPosition().getColumn() + ")} " +
                    "move is invalid");
        }
        else if (gameBoard.getPiece(move.getStartPosition()).getTeamColor() != currentTeam) {
            throw new InvalidMoveException("current team is" + currentTeam.toString() + ", wrong team move");
        }
        else {
            moveMaker(move);
            previousMove = move;
            rookKingHasMoved(move);
            currentTeam = (currentTeam == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE; // <- Me trying to make my code more concise
        }
    }

    /**
     * Function to determine whether the rook or the king has moved yet.
     * @param move is the move that we just made.
     */
    private void rookKingHasMoved (ChessMove move) {
        ChessPiece movedPiece = gameBoard.getPiece(move.getEndPosition());
        if (movedPiece.getPieceType() == ChessPiece.PieceType.KING
         || movedPiece.getPieceType() == ChessPiece.PieceType.ROOK) {

            int startRow = move.getStartPosition().getRow();
            int startCol = move.getStartPosition().getColumn();

            if (startRow == 1) {
                if (startCol == 1) {
                    castlingHistory.setWhiteQueensideRookMoved(true);
                }
                else if (startCol == 5) {
                    castlingHistory.setWhiteKingMoved(true);
                }
                else if (startCol == 8) {
                    castlingHistory.setWhiteKingsideRookMoved(true);
                }
            }
            else if (startRow == 8) {
                if (startCol == 1) {
                    castlingHistory.setBlackQueensideRookMoved(true);
                }
                else if (startCol == 5) {
                    castlingHistory.setBlackKingMoved(true);
                }
                else if (startCol == 8) {
                    castlingHistory.setBlackKingsideRookMoved(true);
                }
            }
        }
    }

    /**
     * A helper function so that if I'm guaranteeing the move is valid already, no exceptions need be thrown.
     * This was made because the tests gave me trouble.
     * @param move is the move to make.
     */
    private void moveMaker(ChessMove move) {
        // directCapture is used for en passant.
        boolean directCapture = false;
        ChessPiece movePiece = gameBoard.getPiece(move.getStartPosition());
        if (move.getPromotionPiece() != null) {
            gameBoard.addPiece(move.getEndPosition(), new ChessPiece(movePiece.getTeamColor(), move.getPromotionPiece()));
        } else {
            if (gameBoard.getPiece(move.getEndPosition()) != null) {
                directCapture = true;
            }
            gameBoard.addPiece(move.getEndPosition(), movePiece);

            // Check for en passant
            if (previousMove != null && enPassantCheck(move.getStartPosition())) {
                makeEnPassant(move, directCapture);
            }

            // Check for castling
            if (movePiece != null && movePiece.getPieceType() == ChessPiece.PieceType.KING) {
                castleMoveMaker(move, movePiece);
            }
        }
        gameBoard.addPiece(move.getStartPosition(), null);
    }

    /**
     * Helper function to assist MoveMaker in performing an En Passant
     * @param move is the move we are making
     * @param directCapture tells us if we are directly capturing a piece rather than doing an en passant
     */
    private void makeEnPassant(ChessMove move, boolean directCapture) {
        if (gameBoard.getPiece(move.getEndPosition()).getPieceType() == ChessPiece.PieceType.PAWN) {
            if (move.getEndPosition().getColumn() == previousMove.getEndPosition().getColumn()){
                if (!directCapture) {
                    ChessPosition capturedPosition = new ChessPosition(move.getEndPosition().getRow() -
                            playDirection(), move.getEndPosition().getColumn());
                    gameBoard.addPiece(capturedPosition, null);
                }
            }
        }
    }

    /**
     * Method to handle castle moves
     * @param move is the move we want to make
     * @param movePiece is the piece that we are moving
     */
    private void castleMoveMaker(ChessMove move, ChessPiece movePiece) {
        TeamColor kingColor = movePiece.getTeamColor();
        int direction = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
    if (kingColor == TeamColor.WHITE && !castlingHistory.isWhiteKingMoved() && Math.abs(direction) == 2) {
            if (direction < 0 && !castlingHistory.isWhiteQueensideRookMoved()) {
                moveMaker(new ChessMove(new ChessPosition(1, 1), new ChessPosition(1, 4)));
            }
            else if (direction > 0 && !castlingHistory.isWhiteKingsideRookMoved()) {
                moveMaker(new ChessMove(new ChessPosition(1, 8), new ChessPosition(1, 6)));
            }
        }
        else if (kingColor == TeamColor.BLACK && !castlingHistory.isBlackKingMoved() && Math.abs(direction) == 2) {
            if (direction < 0 && !castlingHistory.isBlackQueensideRookMoved()) {
                moveMaker(new ChessMove(new ChessPosition(8, 1), new ChessPosition(8, 4)));
            }
            else if (direction > 0 && !castlingHistory.isBlackKingsideRookMoved()) {
                moveMaker(new ChessMove(new ChessPosition(8, 8), new ChessPosition(8, 6)));
            }
        }
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

    public void undoCastleRook(ChessMove move, int direction) {
        if (direction < 0) {
             moveMaker(new ChessMove(new ChessPosition(move.getStartPosition().getRow(), 4),
                    new ChessPosition(move.getStartPosition().getRow(), 1)));
        }
        else if (direction > 0) {
            moveMaker(new ChessMove(new ChessPosition(move.getStartPosition().getRow(), 6),
                    new ChessPosition(move.getStartPosition().getRow(), 8)));
        }
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
        if (gameBoard.getPiece(move.getEndPosition()).getPieceType() == ChessPiece.PieceType.KING) {
            int direction = move.getEndPosition().getColumn() - move.getStartPosition().getColumn();
            if (Math.abs(direction) == 2) {
                undoCastleRook(move, direction);
            }
        }
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
        inCheckDeterminer.setGameBoard(gameBoard);
        return inCheckDeterminer.isInCheck(teamColor);
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

    /**
     * Method to get every move a team could make, disregarding check
     * @param teamColor the team to get all moves of
     * @return a Collection<ChessMove> containing all moves of that team </ChessMove>
     */
    private Collection<ChessMove> getAllTeamMoves(TeamColor teamColor) {
        Collection<ChessMove> allMoves = new HashSet<>();
        ChessPosition checkPosition;
        ChessPiece checkPiece;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                checkPosition = new ChessPosition(row, col);
                checkPiece = gameBoard.getPiece(checkPosition);
                if (checkPiece != null && checkPiece.getTeamColor() == teamColor) {
                    allMoves.addAll(validMoves(checkPosition));
                }
            }
        }
        return allMoves;
    }

    public boolean isInStalemate(TeamColor teamColor) {
        checkStalemate.setGameBoard(gameBoard);
        return checkStalemate.isInStalemate(isInCheck(teamColor), getAllTeamMoves(teamColor));
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        ChessPosition setPosition;
        ChessPiece setPiece;
        castlingHistory.resetHistory();
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