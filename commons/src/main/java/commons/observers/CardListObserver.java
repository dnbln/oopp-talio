package commons.observers;

import commons.Card;
import commons.CardList;

public interface CardListObserver {
    /**
     * Sets the CardList that this observer tracks.
     *
     * @param list The list that the observer tracks.
     */
    void setCardList(CardList list);

    /**
     * Called whenever a card is added.
     *
     * @param newCard The new card.
     */
    void cardAdded(Card newCard);

    /**
     * Called when a card is removed.
     *
     * @param card The removed card.
     */
    void cardRemoved(Card card);

    /**
     * Called when a card is moved within the list.
     *
     * @param card        The card that was moved.
     * @param placedAfter The card that the moved card was placed after,
     *                    or null if the card was moved to the first position in the CardList.
     */
    void cardMoved(Card card, Card placedAfter);

    /**
     * Announces to the observer that the title of the card has changed.
     *
     * @param newTitle The new title.
     */
    void titleSet(String newTitle);

    /**
     * Announces to the observer that the font color has been changed.
     *
     * @param newFontColor The new foreground color.
     */
    void fontColorSet(String newFontColor);

    /**
     * Announces to the observer that the background color has been changed.
     *
     * @param newBackgroundColor The new background color.
     */
    void backgroundColorSet(String newBackgroundColor);
}
