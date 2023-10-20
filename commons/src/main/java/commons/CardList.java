package commons;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import commons.observers.CardListObserver;
import commons.patchExceptions.NoSuchCard;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Transient;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Entity
public final class CardList {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // package-private setId used for testing
    void setId(final long newId) {
        this.id = newId;
    }

    @OneToMany(mappedBy = "cardList", fetch = FetchType.EAGER)
    @OrderColumn(name = "cardSequence")
    private final List<Card> cards;

    @Transient
    @JsonIgnore
    private ArrayList<CardListObserver> observers;

    @Column
    private long board;

    @Column
    private String title;


    @Column
    private String fontColor;

    @Column
    private String backgroundColor;

    void setBoard(final long board) {
        if (this.board != 0 && this.board != board) {
            throw new RuntimeException("setBoard called more than one time");
        }

        this.board = board;
    }

    /**
     * Used on the back-end, to set the board field before saving.
     *
     * @param board The board.
     */
    public void presaveForBoard(final Board board) {
        setBoard(board.getId());
    }

    /**
     * The constructor of the class.
     */
    public CardList() {
        cards = new ArrayList<>();
        title = "";
        fontColor = "";
        backgroundColor = "";
    }

    /**
     * Adds the given observer to the list of
     * observers to notify on changes to this CardList.
     *
     * @param observer The observer.
     */
    public void notify(final CardListObserver observer) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }

        this.observers.add(observer);

        observer.setCardList(this);
    }

    /**
     * Get id.
     *
     * @return id The value to get.
     */
    @JsonGetter("id")
    public long getId() {
        return id;
    }

    /**
     * Get cards.
     *
     * @return cards The value to get.
     */
    @JsonGetter("cards")
    public List<Card> getCards() {
        return cards;
    }

    /**
     * Get the title of the list.
     *
     * @return the title of the list.
     */
    @JsonGetter("title")
    public String getTitle() {
        return title;
    }

    /**
     * Getter for the font color.
     *
     * @return The font color.
     */
    @JsonGetter("fontColor")
    public String getFontColor() {
        return fontColor;
    }

    /**
     * Getter for the background color.
     *
     * @return The background color.
     */
    @JsonGetter("backgroundColor")
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Patches this CardList with the given patcher.
     *
     * @param patcher The patcher.
     */
    public void patch(final Consumer<CardListPatcher> patcher) {
        var p = new CardListPatcher();

        patcher.accept(p);
    }

    private void forEachObserver(final Consumer<CardListObserver> observerConsumer) {
        if (this.observers != null) {
            this.observers.forEach(observerConsumer);
        }
    }

    public final class CardListPatcher {
        /**
         * Gets the card list that we are patching.
         *
         * @return The card list.
         */
        public CardList getBaseCardList() {
            return CardList.this;
        }

        private Card findCard(final long cardId) {
            return CardList.this.cardById(cardId);
        }

        /**
         * Adds a card to this list.
         *
         * @param card The card to add.
         */
        public void addCard(final Card card) {
            CardList.this.cards.add(card);

            card.setCardList(CardList.this.id, false);

            CardList.this.forEachObserver(observer -> observer.cardAdded(card));
        }

        /**
         * Adds a card after a hook Card. The new card will be inserted after the hook card.
         *
         * @param card The card to be added.
         * @param hook The "hook" that the new card will be added after, or
         *             null if at the very beginning of the list.
         */
        public void addCardAfter(final Card card, final Card hook) {
            var newPos = hook == null ? 0 : CardList.this.cards.indexOf(findCard(hook.getId())) + 1;

            CardList.this.cards.add(newPos, card);
        }

        /**
         * Removes a card from the list.
         *
         * @param cardId The card id.
         * @return the removed card.
         */
        public Card removeCard(final long cardId) {
            return removeCard(cardId, true);
        }

        /**
         * Sets the new title of the card list.
         *
         * @param newTitle the new title.
         */
        public void setTitle(final String newTitle) {
            CardList.this.title = newTitle;

            CardList.this.forEachObserver(observer -> observer.titleSet(newTitle));
        }

        /**
         * Removes a card from the list.
         *
         * @param cardId The card id.
         * @param notify Whether to notify observers.
         */
        Card removeCard(final long cardId, final boolean notify) {
            var card = findCard(cardId);

            CardList.this.cards.remove(card);

            if (notify) {
                CardList.this.forEachObserver(observer -> observer.cardRemoved(card));
            }

            return card;
        }

        /**
         * Moves the card identified by the card id, such that it is positioned after
         * the card identified by the placedAfterId.
         *
         * @param cardId        The id of the card to move.
         * @param placedAfterId The id of the card that this card will be placed after.
         *                      Use 0 to move a card to the very beginning of the list.
         */
        public void moveCard(final long cardId, final long placedAfterId) {
            Predicate<Card> placedAfterCardPredicate = (card) -> card.getId() == placedAfterId;

            Card card = findCard(cardId);
            Optional<Card> placedAfter = CardList.this.cards.stream().filter(placedAfterCardPredicate).findFirst();

            CardList.this.cards.remove(card);

            int insertIndex = placedAfter.map(CardList.this.cards::indexOf)
                    .map(it -> it + 1) // insert after, not before
                    .orElse(0);

            CardList.this.cards.add(insertIndex, card);

            CardList.this.forEachObserver(observer -> observer.cardMoved(card, placedAfter.orElse(null)));
        }

        /**
         * Sets the font color.
         *
         * @param newFontColor The new font color.
         */
        public void setFontColor(final String newFontColor) {
            CardList.this.fontColor = newFontColor;

            CardList.this.forEachObserver(o -> o.fontColorSet(newFontColor));
        }

        /**
         * Sets the background color.
         *
         * @param newBackgroundColor The new background color.
         */
        public void setBackgroundColor(final String newBackgroundColor) {
            CardList.this.backgroundColor = newBackgroundColor;

            CardList.this.forEachObserver(o -> o.backgroundColorSet(newBackgroundColor));
        }

        /**
         * Patches a card.
         *
         * @param cardId  The card id of the card to patch.
         * @param patcher The patcher.
         */
        public void patchCard(final long cardId, final Consumer<Card.CardPatcher> patcher) {
            var card = findCard(cardId);

            card.patch(patcher);
        }
    }

    /**
     * Equals method for this class.
     *
     * @param o The other object to check the equality with.
     * @return {@code true} if equal, {@code false} if not equal.
     */
    @Override
    public boolean equals(final Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    /**
     * Tha hashcode for this class.
     *
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getCards(), getFontColor(), getBackgroundColor());
    }

    /**
     * Creates a string of the instance.
     *
     * @return A string representing the instance.
     */
    @Override
    public String toString() {
        return "CardList{" +
                "id=" + id +
                ", cards=" + cards +
                ", observers=" + observers +
                ", board=" + board +
                ", title='" + title + '\'' +
                ", fontColor='" + fontColor + '\'' +
                ", backgroundColor='" + backgroundColor + '\'' +
                '}';
    }

    /**
     * Looks up a card by its id.
     *
     * @param card The card id.
     * @return The card.
     * @throws NoSuchCard If no such card was found.
     */
    public Card cardById(final long card) throws NoSuchCard {
        return this.cards.stream()
                .filter(it -> it.getId() == card)
                .findFirst().orElseThrow(() -> new NoSuchCard(card));
    }
}

