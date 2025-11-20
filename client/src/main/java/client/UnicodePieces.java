package client;

public record UnicodePieces(char[] pieces) {
    public static final UnicodePieces PIECES = new UnicodePieces(new char[] {
            '\u2659', // 0 = white pawn
            '\u2658', // 1 = white knight
            '\u2657', // 2 = white bishop
            '\u2656', // 3 = white rook
            '\u2655', // 4 = white queen
            '\u2654', // 5 = white king
            '\u265F', // 6 = black pawn
            '\u265E', // 7 = black knight
            '\u265D', // 8 = black bishop
            '\u265C', // 9 = black rook
            '\u265B', // 10 = black queen
            '\u265A'  // 11 = black king
    });

    public char getPiece(int index) {
        return pieces[index];
    }
}
