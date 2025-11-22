package client;

public record UnicodePieces(char[] pieces) {
    public static final UnicodePieces PIECES = new UnicodePieces(new char[] {
//            '♙', // 0 = white pawn
//            '♖', // 1 = white rook
//            '♘', // 2 = white knight
//            '♗', // 3 = white bishop
//            '♕', // 4 = white queen
//            '♔', // 5 = white king
//            '♟', // 6 = black pawn
//            '♜', // 7 = black rook
//            '♞', // 8 = black knight
//            '♝', // 9 = black bishop
//            '♛', // 10 = black queen
//            '♚'  // 11 = black king

            'p', // 0 = white pawn
            'r', // 1 = white rook
            'n', // 2 = white knight
            'b', // 3 = white bishop
            'q', // 4 = white queen
            'k', // 5 = white king
            'p', // 6 = black pawn
            'r', // 7 = black rook
            'n', // 8 = black knight
            'b', // 9 = black bishop
            'q', // 10 = black queen
            'k'  // 11 = black king
    });

    public char getPiece(int index) {
        return pieces[index];
    }
}
