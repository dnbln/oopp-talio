package commons.patchExceptions;

import java.util.NoSuchElementException;

public class NoSuchCardList extends NoSuchElementException {
    private final long cardListId;

    /**
     * Constructor.
     *
     * @param cardListId Card list id.
     */
    public NoSuchCardList(final long cardListId) {
        super("No card list with id " + cardListId);
        this.cardListId = cardListId;
    }

    /**
     * Getter for card list id.
     *
     * @return The card list id.
     */
    public long getCardListId() {
        return cardListId;
    }
}
