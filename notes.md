# MY NOTES
---
**CHESSBOARD**

I plan to use a bitboard representation for the board, as explained at https://www.chessprogramming.org/Bitboards

ChessBoard will be a collection of 12 bitboards; one for each piece type.

A LSF-Mapped board has bit 0 as a1 and bit 63 as h8. 

---
**CHESSPOSITION**

To locate a bit index: 
index = 8*rank + file

To find rank and file: 
file = index % 8
rank = index / 8

---
**CHESSPIECE**

ChessPiece will just be a definition of each piece type; this will allow a switch statement to select the correct bitboard
whenever a piece is added.
---
**CHESSMOVE**

When checking if a space is movable, create a function for check_black_obstruction and check_white_obstruction.
Return the bitboard that contains an obstructive piece (if null, space is empty).
There you can stop your raycast, and depending on if it's an attacking move on the opposite team, you can take
the returned bitboard and update it before setting the board again.
_ALTERNATIVELY_, return an integer from 1 to 12 that corresponds to each piece type's bitboard for easier manipulation.
This will enable our switch statements.

Traveling straights is simple. index-8, index-1, index+1, index+8.
Traveling the diagonals is easy. index+7, index+9, index-7, index-9
Of course, we want to make sure that the edges of the board are respected.
That's why we compare our index with the next value. 

Comparisons:
if index % 8 </> checkedPos % 8
if checkedPos < 0 || checkedPos > 63 (think about indexing; will we go 0 to 63 or 1 to 64?)
---
**Move Calculators**

Each piece needs a move calculator. I'll have subclasses for diagMove and straightMove.
