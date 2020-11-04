package com.whg.chess.engine;

import com.whg.chess.engine.factory.BoardFactory;
import com.whg.chess.engine.rule.exceptions.ChessRuleException;
import com.whg.chess.model.*;
import com.whg.chess.model.enums.Color;
import com.whg.chess.model.enums.PieceName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.whg.chess.model.Coordinates.of;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DisplayName("Requirement #5.4: The queen can move any number of squares horizontally, vertically or diagonally.")
class QueenRulesTest {

    @Autowired
    private GameEngine engine;

    @Autowired
    private BoardFactory boardFactory;

    @ParameterizedTest
    @CsvSource({

            // Lines
            "E4,E8, Target is on the most distant square on north",
            "E4,E1, Target is on the most distant square on south",
            "E4,A4, Target is on the most distant square on west",
            "E4,H4, Target is on the most distant square on east",
            "E4,E5, Target is right near the piece on north",
            "E4,E3, Target is right near the piece on south",
            "E4,F4, Target is right near the piece on east",
            "E4,D4, Target is right near the piece on west",

            // Diagonals
            "E4,H7, Long north-east diagonal",
            "E4,H1, Long south-east diagonal",
            "E4,A8, Long north-west diagonal",
            "E4,B1, Long south-west diagonal",
            "E4,D5, Target is right near the piece on north-west diagonal",
            "E4,F5, Target is right near the piece on north-east diagonal",
            "E4,F3, Target is right near the piece on south-east diagonal",
            "E4,D3, Target is right near the piece on south-west diagonal"
    })
    @DisplayName("Queen must reach targets on verticals/horizontals and diagonals")
    void testValidMoves(Coordinates from, Coordinates to, String comment) {
        Board board = boardFactory.getClearBoard();

        setQueen(board, from);
        setOpponentsPiece(board, to);

        Board afterMove = engine.performMove(board, new Move(Color.WHITE, from, to));

        validatePieceCaptured(to, afterMove);
    }

    @ParameterizedTest
    @CsvSource({

            // Lines
            "E4,E8,E5, The piece is on the path north",
            "E4,E1,E2, The piece is on the path south",
            "E4,A4,C4, The piece is on the path west",
            "E4,H4,G4, The piece is on the path east",

            // Diagonals
            "E4,H7,F5, The piece on the path on north-east diagonal",
            "E4,H1,G2, The piece on the path on south-east diagonal",
            "E4,A8,C6, The piece on the path on north-west diagonal",
            "E4,B1,C2, The piece on the path on south-west diagonal"
    })
    @DisplayName("Requirement #8. For pieces other than the knight disallow the move if there are any other pieces in the way between the start and end square.")
    void testPieceOnThePath(Coordinates from, Coordinates to, Coordinates pieceOnPath, String comment) {
        Board board = boardFactory.getClearBoard();

        setQueen(board, from);
        setOpponentsPiece(board, to);
        setOpponentsPiece(board, pieceOnPath);

        ChessRuleException thrown = assertThrows(
                ChessRuleException.class,
                () -> engine.performMove(board, new Move(Color.WHITE, from, to))
        );

        assertThat(thrown.getMessage(), containsString(to + " can't be reached since there is a piece at " + pieceOnPath + " on the path"));
    }

    @Test
    @DisplayName("Target is neither on horizontal nor it is on vertical nor it is on diagonal")
    void testTargetNotReachable() {
        Board board = boardFactory.getClearBoard();

        Coordinates from = of("e4");
        Coordinates to = of("g3");

        setQueen(board, from);
        setOpponentsPiece(board, to);

        ChessRuleException thrown = assertThrows(
                ChessRuleException.class,
                () -> engine.performMove(board, new Move(Color.WHITE, from, to))
        );

        assertThat(thrown.getMessage(), containsString("The queen at E4 can't move to G3 since the target is neither on the same diagonal nor on the same line"));
    }

    private void validatePieceCaptured(Coordinates to, Board afterMove) {
        assertEquals(PieceName.QUEEN, afterMove.getSquare(to).getPiece().getName());

        List<Square> whitePieces = afterMove.getSquaresWithPieces(Color.WHITE);
        assertThat(whitePieces, hasSize(1));

        List<Square> blackPieces = afterMove.getSquaresWithPieces(Color.BLACK);
        assertThat(blackPieces, hasSize(0));
    }

    private void setOpponentsPiece(Board board, Coordinates coordinates) {
        Square squareWithBlackKnight = board.getSquare(coordinates);
        squareWithBlackKnight.setPiece(new Piece(PieceName.KNIGHT, Color.BLACK));
    }

    private void setQueen(Board board, Coordinates coordinates) {
        Square squareWithWhiteRook = board.getSquare(coordinates);
        squareWithWhiteRook.setPiece(new Piece(PieceName.QUEEN, Color.WHITE));
    }

}