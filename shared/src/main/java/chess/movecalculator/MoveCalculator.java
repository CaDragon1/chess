package chess.movecalculator;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * MoveCalculator is an interface that will be used by all move calculators.
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

    private ChessGame.TeamColor occupiedSpace(int index) {throw new RuntimeException("Not implemented");}

    public List<ChessMove> getMoves(){
        return moveList;
    }
}
