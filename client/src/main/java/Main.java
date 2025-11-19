import chess.*;
import client.Repl;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        String serverURL = "http://localhost:8080";

        if (args.length == 1) {
            serverURL = args[0];
        }

        new Repl(serverURL).run();
    }
}