package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * MoveCalculator is a superclass that will be used by all move calculators.
 * It will contain the necessary variables and method declarations for a move calculator.
 * We will create a dynamic list of ChessMove objects for use when calculating all moves.
 */
public class MoveCalculator {
    /** Variables **/
    List<ChessMove> moveList = new ArrayList<>();
    ChessBoard board;
    ChessPosition position;

    MoveCalculator(ChessBoard board, ChessPosition position) {
        this.board = board;
        this.position = position;
    }

    /**
     * Methods
     **/
    private void calculateMoves() {throw new RuntimeException("Not implemented");}

    /**
     * checkBoundaries ensures that the index does not exceed the current boundaries.
     * @param index is the starting index
     * @param checking is the index we're testing
     * @return true if checking is out of bounds, false if within bounds.
     */
    public boolean checkBoundaries(int index, int checking) {
        int startingCol = index % 8;
        int nextCol = checking % 8;

        if (checking == index || checking < 0 || checking > 63) {
            return true;
        }
        if (Math.abs(nextCol - startingCol) > 1) {
            return true;
        }
        return false;
    }

    private ChessGame.TeamColor occupiedSpace(int index) {throw new RuntimeException("Not implemented");}

    public List<ChessMove> getMoves(){
        return moveList;
    }
}
