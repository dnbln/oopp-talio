package commons.patchExceptions;

import java.util.NoSuchElementException;

public class NoSuchCard extends NoSuchElementException {
    private final long cardId;

    /**
     * Constructor.
     *
     * @param cardId Card id.
     */
    public NoSuchCard(final long cardId) {
        super("No card with id " + cardId);
        this.cardId = cardId;
    }

    /**
     * Getter for card id.
     *
     * @return Card id.
     */
    public long getCardId() {
        return cardId;
    }
}
