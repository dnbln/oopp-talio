package commons;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CardListTest {

    private final CardList cardList = new CardList();

    @Test
    void testGetId() {
        assertEquals(0, cardList.getId());
    }

    @Test
    void testGetCardList() {
        assertNotNull(cardList.getCards());
        assertTrue(cardList.getCards().isEmpty());
    }

    @Test
    void testToString() {
        assertEquals("CardList{id=0, cards=[], observers=null, board=0, title=''," +
                " fontColor='', backgroundColor=''}", cardList.toString());
    }

    @Test
    void testAddCard() {
        var card = new Card();
        cardList.patch(patcher -> patcher.addCard(card));

        assertFalse(cardList.getCards().isEmpty());
    }

    @Test
    void testRemoveCard() {
        var card = new Card();
        cardList.patch(patcher -> patcher.addCard(card));

        assertFalse(cardList.getCards().isEmpty());

        cardList.patch(patcher -> patcher.removeCard(card.getId()));

        assertTrue(cardList.getCards().isEmpty());
    }

    @Test
    void testMoveCards() {
        var cardA = new Card();
        cardA.setId(1);
        var cardB = new Card();
        cardB.setId(2);
        var cardC = new Card();
        cardC.setId(3);

        cardList.patch(cardListPatcher -> {
            cardListPatcher.addCard(cardA);
            cardListPatcher.addCard(cardB);
            cardListPatcher.addCard(cardC);
        });

        assertEquals(cardList.getCards(), new ArrayList<>(List.of(cardA, cardB, cardC)));

        cardList.patch(cardListPatcher -> {
            cardListPatcher.moveCard(cardC.getId(), 0); // move card C to the beginning
        });

        assertEquals(cardList.getCards(), new ArrayList<>(List.of(cardC, cardA, cardB)));

        cardList.patch(cardListPatcher -> {
            cardListPatcher.moveCard(cardB.getId(), cardC.getId()); // move cardB after cardC
        });

        assertEquals(cardList.getCards(), new ArrayList<>(List.of(cardC, cardB, cardA)));
    }
}
