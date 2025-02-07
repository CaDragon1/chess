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
    boolean whiteKingsideRookMoved;
    boolean whiteQueensideRookMoved;
    boolean blackKingsideRookMoved;
    boolean blackQueensideRookMoved;
    boolean whiteKingMoved;
    boolean blackKingMoved;

    public ChessGame() {
        currentTeam = TeamColor.WHITE;
        gameBoard = new ChessBoard();
        gameBoard.resetBoard();
        previousMove = null;

        whiteKingMoved = false;
        whiteKingsideRookMoved = false;
        whiteQueensideRookMoved = false;
        blackKingMoved = false;
        blackKingsideRookMoved = false;
        blackQueensideRookMoved = false;
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
            System.out.println("- Getting valid moves for " + gameBoard.getPiece(startPosition).getTeamColor().toString() + " " + gameBoard.getPiece(startPosition).getPieceType().toString());
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
            System.out.println("    - Start position: " + startPosition.getColumn());
            System.out.println("    - Is in check: " + isInCheck(gameBoard.getPiece(startPosition).getTeamColor()));
            if (gameBoard.getPiece(startPosition).getPieceType() == ChessPiece.PieceType.KING
                    && startPosition.getColumn() == 5
                    && !isInCheck(gameBoard.getPiece(startPosition).getTeamColor())) {
                System.out.println("Adding castling moves!");
                pieceMoves.addAll(getCastleMoves(startPosition));
            }

            return pieceMoves;
        }
        else return null;
    }

    /**
     * A function to return all possible castle moves at the given position
     * @param startPosition is the position of the king.
     * @return a collection of all possible moves the King can make when castling.
     */
    private Collection<ChessMove> getCastleMoves (ChessPosition startPosition) {
        Collection<ChessMove> castleMoves = new HashSet<>();
        ChessPiece king = gameBoard.getPiece(startPosition);
        
        if (king.getTeamColor() == TeamColor.WHITE && !whiteKingMoved) {
            if (!whiteKingsideRookMoved && checkPathClear(startPosition, 1)) {
                castleMoves.add(new ChessMove(startPosition, new ChessPosition(1, 7)));
            }
            if (!whiteQueensideRookMoved && checkPathClear(startPosition, -1)) {
                castleMoves.add(new ChessMove(startPosition, new ChessPosition(1, 3)));
            }
        }
        else if (king.getTeamColor() == TeamColor.BLACK && !blackKingMoved) {
            if (!blackKingsideRookMoved && checkPathClear(startPosition, 1)) {
                castleMoves.add(new ChessMove(startPosition, new ChessPosition(8, 7)));
            }
            if (!blackQueensideRookMoved && checkPathClear(startPosition, -1)) {
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
     * @return
     */
    private boolean enPassantCheck (ChessPosition startPosition) {
        if (previousMove != null) {
            ChessPiece checkPiece = gameBoard.getPiece(previousMove.getEndPosition());
            int previousMoveRow = previousMove.getEndPosition().getRow();
            int previousMoveCol = previousMove.getEndPosition().getColumn();

            // Our piece must be a pawn. The previous move must have been a pawn.
            // The positions must align, and the previous pawn must have made a double move off their starting rank.
            if (checkPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if (previousMove.getStartPosition().getRow() == (previousMoveRow + (2 * playDirection()))
                        && gameBoard.getPiece(startPosition).getPieceType() == ChessPiece.PieceType.PAWN
                        && startPosition.getRow() == previousMoveRow
                        && (startPosition.getColumn() == previousMoveCol + 1 || startPosition.getColumn() == previousMoveCol - 1)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines the direction of play.
     * @return 1 if white, -1 if black
     */
    private int playDirection() {
        if (currentTeam == TeamColor.WHITE) return 1;
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
            throw new InvalidMoveException("move is invalid");
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
                    whiteQueensideRookMoved = true;
                }
                else if (startCol == 5) {
                    whiteKingMoved = true;
                }
                else if (startCol == 8) {
                    whiteKingsideRookMoved = true;
                }
            }
            else if (startRow == 8) {
                if (startCol == 1) {
                    blackQueensideRookMoved = true;
                }
                else if (startCol == 5) {
                    blackKingMoved = true;
                }
                else if (startCol == 8) {
                    blackKingsideRookMoved = true;
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
            if (previousMove != null && enPassantCheck(move.getStartPosition())) {
                if (gameBoard.getPiece(move.getEndPosition()).getPieceType() == ChessPiece.PieceType.PAWN) {
                    if (move.getEndPosition().getColumn() == previousMove.getEndPosition().getColumn()){
                        if (!directCapture) {
                            ChessPosition capturedPosition = new ChessPosition(move.getEndPosition().getRow() - playDirection(), move.getEndPosition().getColumn());
                            gameBoard.addPiece(capturedPosition, null);
                        }
                    }
                }
            }
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
                return true;
            }
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

        // Create two example moves that target the king's position. One for generic pieces,
        // one for pawn promotion moves.
        ChessMove dangerMove = new ChessMove(checkPosition, kingPosition);
        ChessMove pawnDangerMove = new ChessMove(checkPosition, kingPosition, ChessPiece.PieceType.QUEEN);

        // If the danger move is in the move list, the king is in check.
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



    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            if (getAllTeamMoves(teamColor).isEmpty()) {
                return true;
            }
            return isGarbageBoardState();
        }
        return false;
    }

    /**
     * This hideous function just determines whether the board is in an unplayable state.
     * There are a few board states like that, so we check for all of them.
     * This wasn't fun to write.
     * @return true if the board state sucks. Congrats to the players, you are evenly dumb. Otherwise, false.
     */
    private boolean isGarbageBoardState() {
        if (containsNoLimitMovers()) {
            return false;
        }
        else {
            Collection<ChessPosition> whitePieces = getTeamPieces(TeamColor.WHITE);
            Collection<ChessPosition> blackPieces = getTeamPieces(TeamColor.BLACK);

            // Calculate total pieces
            int totalPieces = whitePieces.size() + blackPieces.size();
            if (totalPieces == 2) {
                return true;
            }
            else {
                Collection<ChessPosition> whiteBishops = extractPieces(whitePieces, ChessPiece.PieceType.BISHOP);
                Collection<ChessPosition> blackBishops = extractPieces(blackPieces, ChessPiece.PieceType.BISHOP);
                whitePieces.removeAll(whiteBishops);
                blackPieces.removeAll(blackBishops);

                Collection<ChessPosition> whiteKnights = extractPieces(whitePieces, ChessPiece.PieceType.KNIGHT);
                Collection<ChessPosition> blackKnights = extractPieces(blackPieces, ChessPiece.PieceType.KNIGHT);
                whitePieces.removeAll(whiteKnights);
                blackPieces.removeAll(blackKnights);
                // The remaining piece in whitePieces and blackPieces is the king.

                // board state: King vs King and Bishop OR King vs King and Knight
                if (totalPieces == 3 && (whiteBishops.size() + blackBishops.size() +
                        whiteKnights.size() + blackKnights.size() == 1)) return true;

                // board state: King vs King and all bishops are on the same color
                else if (whiteKnights.isEmpty() && blackKnights.isEmpty()){
                    boolean blackBishopsSameColor = onSameColor(blackBishops);
                    boolean whiteBishopsSameColor = onSameColor(whiteBishops);
                    if (whiteBishops.isEmpty() && !blackBishops.isEmpty() && onSameColor(blackBishops)) {
                        return true;
                    }
                    else if (blackBishops.isEmpty() && !whiteBishops.isEmpty() &&onSameColor(whiteBishops)) {
                        return true;
                    }
                    else if (!blackBishops.isEmpty() && onSameColor(whiteBishops) && onSameColor(blackBishops)) {
                        return true;
                    }
                }

                // board state: King and two knights vs king
                else {
                    if ((whiteKnights.size() == 2 && whiteBishops.isEmpty() && blackKnights.isEmpty() && blackBishops.isEmpty()) ||
                            (blackKnights.size() == 2 && blackBishops.isEmpty() && whiteKnights.isEmpty() && whiteBishops.isEmpty())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Useful for seeing if all bishops are stuck on one color of square. helps determine garbage board states.
     * @param pieces
     * @return
     */
    private boolean onSameColor(Collection<ChessPosition> pieces) {
        int blackSquare = 0;
        int whiteSquare = 0;
        for (ChessPosition piece : pieces) {
            if (piece.getColumn()%2 == 0) {
                if (piece.getRow()%2 == 0) {
                    whiteSquare++;
                }
                else {
                    blackSquare++;
                }
            }
            else {
                if (piece.getRow()%2 == 0) {
                    blackSquare++;
                }
                else {
                    whiteSquare++;
                }
            }
        }
        return whiteSquare == 0 || blackSquare == 0;
    }

    /**
     * Method to extract all pieces of a given type from a collection of positions.
     * @param allPositions Every position passed to the function
     * @param type The piece type we want to return every existing instance of
     * @return A list of all positions containing that piece type
     */
    private Collection<ChessPosition> extractPieces(Collection<ChessPosition> allPositions, ChessPiece.PieceType type) {
        Collection<ChessPosition> pieces = new HashSet<>();
        for (ChessPosition space : allPositions) {
            if (gameBoard.getPiece(space).getPieceType() == type) {
                pieces.add(space);
            }
        }
        return pieces;
    }

    /**
     * Method to make sure there are pieces that have the potential to cover the whole board present.
     * @return true if there are, false otherwise
     */
    private boolean containsNoLimitMovers() {
        ChessPosition checkPosition;
        ChessPiece checkPiece;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                checkPosition = new ChessPosition(row, col);
                checkPiece = gameBoard.getPiece(checkPosition);
                if (checkPiece != null) {
                    if (checkPiece.getPieceType() == ChessPiece.PieceType.PAWN ||
                            checkPiece.getPieceType() == ChessPiece.PieceType.QUEEN ||
                            checkPiece.getPieceType() == ChessPiece.PieceType.ROOK) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * A method to get all piece positions from one team that are on the board. This was simpler than going through every chess move.
     * Its purpose is to help determine whether a boardstate is trash.
     * @return a collection of all occupied positions of one team
     */
    private Collection<ChessPosition> getTeamPieces(TeamColor color) {
        Collection<ChessPosition> allPositions = new HashSet<>();
        ChessPosition checkPosition;
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                checkPosition = new ChessPosition(row, col);
                if (gameBoard.getPiece(checkPosition) != null &&
                        gameBoard.getPiece(checkPosition).getTeamColor() == color) {
                    allPositions.add(checkPosition);
                }
            }
        }
        return allPositions;
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
