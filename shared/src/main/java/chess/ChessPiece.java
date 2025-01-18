package chess;

import chess.pieceMoves.*;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private ChessGame.TeamColor teamColor;
    private ChessPiece.PieceType pieceType;
    private PieceMoves pieceMoves;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        teamColor = pieceColor;
        pieceType = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        switch (board.getPiece(myPosition).getPieceType()) {
            case PAWN:
                pieceMoves = new PawnMoves(board, myPosition);
                return pieceMoves.getMoveList();
            case ROOK:
                pieceMoves = new RookMoves(board, myPosition);
                return pieceMoves.getMoveList();
            case KNIGHT:
                pieceMoves = new KnightMoves(board, myPosition);
                return pieceMoves.getMoveList();
            case BISHOP:
                pieceMoves = new BishopMoves(board, myPosition);
                return pieceMoves.getMoveList();
            case QUEEN:
                pieceMoves = new QueenMoves(board, myPosition);
                return pieceMoves.getMoveList();
            case KING:
                pieceMoves = new KingMoves(board, myPosition);
                return pieceMoves.getMoveList();
            default:
                throw new RuntimeException("Not implemented");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessPiece that)) {
            return false;
        }
        return teamColor == that.teamColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, pieceType);
    }
}
