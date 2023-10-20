package commons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class BoardTest {

    private final Board board = new Board();

    @Test
    void testGetId() {
        Assertions.assertEquals(0, this.board.getId(), "id should be 0");
    }

    @Test
    void testGetCardLists() {
        Assertions.assertNotNull(this.board.getCardLists(), "card lists should not be null");
        Assertions.assertTrue(this.board.getCardLists().isEmpty(), "card lists should be empty");
    }

    @Test
    void testToString() {
        Assertions.assertEquals("Board{id=0, cardLists=[], tags=[], title=''}", this.board.toString(),
                "toString should be correct");
    }

    @Test
    void testAddCardList() {
        this.board.patch(patcher -> patcher.addCardList(new CardList()));

        Assertions.assertFalse(this.board.getCardLists().isEmpty(), "card list should be added");
    }

    @Test
    void testRemoveCardList() {
        var list = new CardList();

        this.board.patch(patcher -> patcher.addCardList(list));

        Assertions.assertFalse(this.board.getCardLists().isEmpty(), "card list should be added");

        this.board.patch(patcher -> patcher.removeCardList(list.getId()));

        Assertions.assertTrue(this.board.getCardLists().isEmpty(), "card list should be removed");
    }

    @Test
    void testMoveCardList() {
        var listA = new CardList();
        listA.setId(1);
        var listB = new CardList();
        listB.setId(2);
        var listC = new CardList();
        listC.setId(3);

        this.board.patch(patcher -> {
            patcher.addCardList(listA);
            patcher.addCardList(listB);
            patcher.addCardList(listC);
        });

        Assertions.assertEquals(this.board.getCardLists(), new ArrayList<>(List.of(listA, listB, listC)),
                "lists should be in order");

        this.board.patch(boardPatcher -> {
            boardPatcher.moveCardList(listB.getId(), 0); // move listB to be the first list.
        });

        Assertions.assertEquals(this.board.getCardLists(), new ArrayList<>(List.of(listB, listA, listC)),
                "listB should be first");

        this.board.patch(boardPatcher -> {
            boardPatcher.moveCardList(listC.getId(), listB.getId()); // move listC after listB
        });

        Assertions.assertEquals(this.board.getCardLists(), new ArrayList<>(List.of(listB, listC, listA)),
                "listC should be after listB");
    }

    @Test
    void testGetTitle() {
        Assertions.assertEquals("", this.board.getTitle(), "default title should be empty");
        // default title is empty string
    }

    @Test
    void testGetFontColor() {
        Assertions.assertNull(this.board.getFontColor(),
                "default font color should be empty"); // default font color is empty string
    }

    @Test
    void getBackgroundColor() {
        Assertions.assertNull(this.board.getBackgroundColor(),
                "default background color should be empty"); // default background color is empty string
    }

    @Test
    void testGetDefaultCardColorPreset() {
        Assertions.assertEquals(0, this.board.getDefaultCardColorPreset(),
                "default card color preset should be 0"); // default card color preset is 0
    }

    @Test
    void testGetPresets() {
        Assertions.assertNotNull(this.board.getPresets(), "presets should not be null");
        Assertions.assertTrue(this.board.getPresets().isEmpty(), "presets should be empty");
    }

    @Test
    void testEquals() {
        Board board1 = new Board();
        Board board2 = new Board();
        Board board3 = new Board();
        board3.patch(patcher -> patcher.addCardList(new CardList()));

        Assertions.assertEquals(board1, board1, "should be reflexive"); // reflexive property
        Assertions.assertTrue(board1.equals(board2) && board2.equals(board1),
                "Should be symmetric"); // symmetric property
        Assertions.assertNotNull(board1, "null check"); // null check
        Assertions.assertNotEquals(new Object(), board1, "type check"); // type check
        Assertions.assertNotEquals(board1, board3, "equality check"); // equality check
    }

    @Test
    void testHashCode() {
        Board board1 = new Board();
        Board board2 = new Board();
        Board board3 = new Board();
        board3.patch(patcher -> patcher.addCardList(new CardList()));

        Assertions.assertEquals(board1.hashCode(), board1.hashCode(), "should be reflexive"); // reflexive property
        Assertions.assertEquals(board1.hashCode(), board2.hashCode(), "Should be symmetric"); // symmetric property
        Assertions.assertNotEquals(board1.hashCode(), board3.hashCode(), "equality check"); // equality check
    }
}

