package chess;

public class CastlingHistory {
    boolean whiteKingsideRookMoved;
    boolean whiteQueensideRookMoved;
    boolean blackKingsideRookMoved;
    boolean blackQueensideRookMoved;
    boolean whiteKingMoved;
    boolean blackKingMoved;

    CastlingHistory() {
        whiteKingMoved = false;
        whiteKingsideRookMoved = false;
        whiteQueensideRookMoved = false;
        blackKingMoved = false;
        blackKingsideRookMoved = false;
        blackQueensideRookMoved = false;
    }

    public void resetHistory() {
        whiteKingMoved = false;
        whiteKingsideRookMoved = false;
        whiteQueensideRookMoved = false;
        blackKingMoved = false;
        blackKingsideRookMoved = false;
        blackQueensideRookMoved = false;
    }

    public boolean isWhiteKingsideRookMoved() {
        return whiteKingsideRookMoved;
    }

    public void setWhiteKingsideRookMoved(boolean whiteKingsideRookMoved) {
        this.whiteKingsideRookMoved = whiteKingsideRookMoved;
    }

    public boolean isWhiteQueensideRookMoved() {
        return whiteQueensideRookMoved;
    }

    public void setWhiteQueensideRookMoved(boolean whiteQueensideRookMoved) {
        this.whiteQueensideRookMoved = whiteQueensideRookMoved;
    }

    public boolean isBlackKingsideRookMoved() {
        return blackKingsideRookMoved;
    }

    public void setBlackKingsideRookMoved(boolean blackKingsideRookMoved) {
        this.blackKingsideRookMoved = blackKingsideRookMoved;
    }

    public boolean isBlackQueensideRookMoved() {
        return blackQueensideRookMoved;
    }

    public void setBlackQueensideRookMoved(boolean blackQueensideRookMoved) {
        this.blackQueensideRookMoved = blackQueensideRookMoved;
    }

    public boolean isWhiteKingMoved() {
        return whiteKingMoved;
    }

    public void setWhiteKingMoved(boolean whiteKingMoved) {
        this.whiteKingMoved = whiteKingMoved;
    }

    public boolean isBlackKingMoved() {
        return blackKingMoved;
    }

    public void setBlackKingMoved(boolean blackKingMoved) {
        this.blackKingMoved = blackKingMoved;
    }
}
