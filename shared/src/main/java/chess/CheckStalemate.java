package chess;

import java.util.Collection;
import java.util.HashSet;

public class CheckStalemate {

    private ChessBoard gameBoard;

    CheckStalemate (ChessBoard gameBoard) {
        this.gameBoard = gameBoard;
    }
    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param isInCheck If the team whose list of moves is passed in is in check
     * @param teamMoves The entire movelist of whichever team we want to test
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(boolean isInCheck, Collection<ChessMove> teamMoves) {
        if (!isInCheck) {
            if (teamMoves.isEmpty()) {
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
            Collection<ChessPosition> whitePieces = getTeamPieces(ChessGame.TeamColor.WHITE);
            Collection<ChessPosition> blackPieces = getTeamPieces(ChessGame.TeamColor.BLACK);

            // Calculate total pieces
            int totalPieces = whitePieces.size() + blackPieces.size();
            if (totalPieces == 2) {
                return true;
            }
            else {
                return pieceExtraction(whitePieces, blackPieces, totalPieces);
            }
        }
    }

    /**
     * This is why the previous function is hideous. This is one ugly chunk of code.
     * @param whitePieces is a list of all remaining white pieces
     * @param blackPieces is a list of all remaining black pieces
     * @param totalPieces is the total number of pieces remaining on the board
     * @return true if the board state is truly abhorrent
     */
    private boolean pieceExtraction(Collection<ChessPosition> whitePieces, Collection<ChessPosition> blackPieces, int totalPieces) {
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
                whiteKnights.size() + blackKnights.size() == 1)) {
            return true;
        }

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
            else {
                return !blackBishops.isEmpty() && onSameColor(whiteBishops) && onSameColor(blackBishops);
            }
        }

        // board state: King and two knights vs king
        else {
            return (whiteKnights.size() == 2 && whiteBishops.isEmpty() && blackKnights.isEmpty() && blackBishops.isEmpty()) ||
                    (blackKnights.size() == 2 && blackBishops.isEmpty() && whiteKnights.isEmpty() && whiteBishops.isEmpty());
        }
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
    private Collection<ChessPosition> getTeamPieces(ChessGame.TeamColor color) {
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

    public void setGameBoard(ChessBoard gameBoard) {
        this.gameBoard = gameBoard;
    }
}
