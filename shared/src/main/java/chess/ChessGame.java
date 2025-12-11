package chess;

import java.util.ArrayList;
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
    private ChessBoard gameBoard;
    private TeamColor currentTeamTurn;

    public ChessGame() {
        gameBoard = new ChessBoard();
        gameBoard.resetBoard();
        currentTeamTurn = TeamColor.WHITE;

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTeamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        currentTeamTurn = team;
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
        ChessPiece piece = gameBoard.getPiece(startPosition.getIndex());

        if (piece != null) {
            ChessGame.TeamColor teamColor = piece.getTeamColor();
            Collection<ChessMove> pieceMoves = piece.pieceMoves(gameBoard, startPosition);
            return validMoveLoop(teamColor, pieceMoves);
        }
        return null;
    }

    private Collection<ChessMove> validMoveLoop(TeamColor teamColor, Collection<ChessMove> moves) {
        ChessBoard boardCopy = new ChessBoard();
        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : moves) {
            boardCopy.copy(gameBoard);
            try {
                boardCopy = makeMove(move, boardCopy);
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }
            if (!isBoardInCheck(teamColor, boardCopy)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    // I asked an AI to evaluate my allValidMoves function in terms of logical consistency and efficiency.
    // It seems there may be a better way of doing this using bitboards; I may implement a change in the future,
    // but this will suffice for now.
    private Collection<ChessMove> allValidMoves(TeamColor teamColor) {
        Collection<ChessMove> teamMoves = allTeamMoves(teamColor, gameBoard);
        return validMoveLoop(teamColor, teamMoves);
    }

    /**
     * Gets all team moves possible before checking for check, castling, etc.
     * @param teamColor the team we're getting moves for
     * @param board the board we want to get moves from
     * @return a collection of ChessMove objects containing every possible move from that team
     */
    private Collection<ChessMove> allTeamMoves(TeamColor teamColor, ChessBoard board) {
        int startingIndex = (teamColor == TeamColor.WHITE ? 0 : 6);
        ChessPiece checkingPiece;
        long[] gameBitboards = board.getBitboards();
        Collection<ChessMove> possibleMoves = new HashSet<>();
        ChessPosition checkPosition;

        for (int i = startingIndex; i < startingIndex + 6; i++) {
            long bitboard = gameBitboards[i];
            checkingPiece = determinePiece(i, teamColor);
            // I got this code idea from https://alexharri.com/blog/bit-set-iteration; I'm trying to learn bitwise
            // operators, and two's complement makes this an efficient way of cycling through only our needed bits.
            // Took a while for me to understand the premise, though.
            while (bitboard != 0) {
                // lsb == least significant bit
                int lsb = (Long.numberOfTrailingZeros(bitboard & -bitboard));
                checkPosition = new ChessPosition(lsb);
                possibleMoves.addAll(checkingPiece.pieceMoves(board, checkPosition));
                bitboard ^= (1L << lsb);
            }
        }
        return possibleMoves;
    }

    private ChessPiece determinePiece(int index, TeamColor teamColor) {
        return switch (index % 6) {
            case 0 -> new ChessPiece(teamColor, ChessPiece.PieceType.PAWN);
            case 5 -> new ChessPiece(teamColor, ChessPiece.PieceType.ROOK);
            case 4 -> new ChessPiece(teamColor, ChessPiece.PieceType.KNIGHT);
            case 3 -> new ChessPiece(teamColor, ChessPiece.PieceType.BISHOP);
            case 2 -> new ChessPiece(teamColor, ChessPiece.PieceType.QUEEN);
            case 1 -> new ChessPiece(teamColor, ChessPiece.PieceType.KING);
            default -> throw new IllegalStateException("Unexpected value: " + index % 6);
        };
    }

    private int getBitboardIndex(ChessPiece piece) {
        int teamModifier = (piece.getTeamColor() == TeamColor.WHITE ? 0 : 6);
        int indexNum = -11;
        switch (piece.getPieceType()) {
            case KING -> indexNum = 5;
            case QUEEN -> indexNum = 4;
            case BISHOP -> indexNum = 3;
            case KNIGHT -> indexNum = 2;
            case ROOK -> indexNum = 1;
            case PAWN -> indexNum = 0;
        }
        return indexNum + teamModifier;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece movePiece = gameBoard.getPiece(move.getStartPosition().getIndex());
        if (movePiece != null && currentTeamTurn == movePiece.getTeamColor()
        && validMoves(move.getStartPosition()).contains(move)) {
            gameBoard = makeMove(move, gameBoard);
            switchTeam();
        }
        else {
            throw new InvalidMoveException("Invalid move attempted");
        }
    }

    public ChessBoard makeMove(ChessMove move, ChessBoard board) throws InvalidMoveException {
        int startIndex = move.getStartPosition().getIndex();
        int endIndex = move.getEndPosition().getIndex();
        ChessPiece movePiece = board.getPiece(startIndex);
        ChessPiece targetPiece = board.getPiece(endIndex);

        if (movePiece != null && move.getPromotionPiece() == null) {
            gameBoard.setEnPassant(-1);
            return tryNullPromoMove(board, movePiece, startIndex, endIndex, targetPiece);
        }
        else if (movePiece != null && move.getPromotionPiece() != null) {
            gameBoard.setEnPassant(-1);
            return tryPromotionMove(move, board, movePiece, startIndex, endIndex, targetPiece);
        }
        else {
            throw new InvalidMoveException("Starting piece is null");
        }
    }

    private ChessBoard tryPromotionMove(ChessMove move, ChessBoard board, ChessPiece movePiece,
                                        int startIndex, int endIndex, ChessPiece targetPiece) {
        int pieceBBIndex = getBitboardIndex(movePiece);
        board.getBitboards()[pieceBBIndex] &= ~(1L << startIndex);

        pieceBBIndex = getBitboardIndex(new ChessPiece(currentTeamTurn, move.getPromotionPiece()));
        board.getBitboards()[pieceBBIndex] |= (1L << endIndex);

        if (targetPiece != null) {
            int targetBBIndex = getBitboardIndex(targetPiece);
            board.getBitboards()[targetBBIndex] &= ~(1L << endIndex);
        }
        return board;
    }

    private ChessBoard tryNullPromoMove(ChessBoard board, ChessPiece movePiece, int startIndex,
                                        int endIndex, ChessPiece targetPiece) {
        int pieceBBIndex = getBitboardIndex(movePiece);

        // Set the index on the piece's bitboard
        board.getBitboards()[pieceBBIndex] &= ~(1L << startIndex);
        board.getBitboards()[pieceBBIndex] |= (1L << endIndex);

        // Check for and set the index on the targeted piece's bitboard
        if (targetPiece != null) {
            int targetBBIndex = getBitboardIndex(targetPiece);
            board.getBitboards()[targetBBIndex] &= ~(1L << endIndex);
        }
        return board;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isBoardInCheck(teamColor, gameBoard);
    }

    /**
     * Takes a given board and determines whether the boardstate is in check.
     * isInCheck calls this because I'm not allowed to change that method signature.
     *
     * @param teamColor is the team to check for check
     * @param checkBoard is the board we are testing
     * @return True if the team is in check
     */
    private boolean isBoardInCheck(TeamColor teamColor, ChessBoard checkBoard) {
        int kingBBIndex = (teamColor == TeamColor.WHITE ? 5 : 11);
        long kingBitboard = checkBoard.getBitboards()[kingBBIndex];
        int kingIndex = Long.numberOfTrailingZeros(kingBitboard & -kingBitboard);

        Collection<ChessMove> enemyMoves = allTeamMoves(teamColor == TeamColor.WHITE ?
                TeamColor.BLACK : TeamColor.WHITE, checkBoard);
        for (ChessMove checkMove : enemyMoves) {
            if (checkMove.getEndPosition().getIndex() == kingIndex) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {

            ChessBoard boardCopy = new ChessBoard();
            Collection<ChessMove> futureMoves = allTeamMoves(teamColor, gameBoard);

            for (ChessMove i : futureMoves) {
                boardCopy.copy(gameBoard);
                try {
                    boardCopy = makeMove(i, boardCopy);
                } catch (InvalidMoveException e) {
                    throw new RuntimeException(e);
                }
                if (!isBoardInCheck(teamColor, boardCopy)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }
        Collection<ChessMove> validMoves = allValidMoves(teamColor);
        return validMoves.isEmpty();
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        gameBoard.copy(board);
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return gameBoard;
    }

    private void switchTeam() {
        currentTeamTurn = (currentTeamTurn == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(gameBoard, chessGame.gameBoard) && currentTeamTurn == chessGame.currentTeamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameBoard, currentTeamTurn);
    }
}