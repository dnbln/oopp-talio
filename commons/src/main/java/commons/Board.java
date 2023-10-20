package commons;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import commons.observers.BoardObserver;
import commons.patchExceptions.NoSuchCard;
import commons.patchExceptions.NoSuchCardList;
import commons.patchExceptions.NoSuchColorPreset;
import commons.patchExceptions.NoSuchSubtask;
import commons.patchExceptions.NoSuchTag;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Transient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Represents a board in the Talio application. A board consists of one or more lists,
 * and has a title. It can also have multiple observers that will be notified of changes to the board.
 */
@Entity
public final class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(mappedBy = "board", fetch = FetchType.EAGER)
    @OrderColumn(name = "listsSequence")
    private List<CardList> cardLists;

    @OneToMany(mappedBy = "boardId", fetch = FetchType.EAGER)
    private List<Tag> tags;

    @Transient
    @JsonIgnore
    private ArrayList<BoardObserver> observers;

    @Column
    private String title;

    @Column
    private String fontColor;

    @Column
    private String backgroundColor;

    @Column
    private long defaultCardColorPreset;

    @OneToMany(mappedBy = "board", fetch = FetchType.EAGER)
    @OrderColumn(name = "presetsSequence")
    private Collection<ColorPreset> presets;

    /**
     * The constructor for the board.
     */
    public Board() {
        this.cardLists = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.title = "";
        this.presets = new ArrayList<>();
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
     * getter for the title.
     *
     * @return the title of the board.
     */
    @JsonGetter("title")
    public String getTitle() {
        return title;
    }


    /**
     * Get cardLists.
     *
     * @return cardLists The value to get.
     */
    @JsonGetter("cardLists")
    public List<CardList> getCardLists() {
        return cardLists;
    }

    /**
     * Get tags.
     *
     * @return The list of tags.
     */
    @JsonGetter("tags")
    public List<Tag> getTags() {
        return tags;
    }

    /**
     * Get font color.
     *
     * @return font color.
     */
    @JsonGetter("fontColor")
    public String getFontColor() {
        return fontColor;
    }

    /**
     * Get background color.
     *
     * @return Background color.
     */
    @JsonGetter("backgroundColor")
    public String getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Getter for the id of the default card preset.
     *
     * @return The id of the default card preset.
     */
    @JsonGetter("defaultCardColorPreset")
    public long getDefaultCardColorPreset() {
        return defaultCardColorPreset;
    }

    /**
     * Getter for presets.
     *
     * @return The list of presets.
     */
    @JsonGetter("presets")
    public Collection<ColorPreset> getPresets() {
        return presets;
    }

    /**
     * Adds the given observer to the list of observers to notify on changes to the board.
     *
     * @param observer The observer.
     */
    public void notify(final BoardObserver observer) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }

        this.observers.add(observer);
        observer.setBoard(this);
    }

    /**
     * Equals method for this class.
     *
     * @param o The other object to check the equality with.
     * @return {@code true} if equal, {@code false} if not equal.
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Board board)) {
            return false;
        }
        return getId() == board.getId() && Objects.equals(getCardLists(), board.getCardLists()) &&
               Objects.equals(getTitle(), board.getTitle()) && Objects.equals(getTags(), board.getTags()) &&
               Objects.equals(getFontColor(), board.getFontColor()) &&
               Objects.equals(getBackgroundColor(), board.getBackgroundColor()) &&
               getDefaultCardColorPreset() == board.getDefaultCardColorPreset();
    }

    /**
     * Tha hashcode for this class.
     *
     * @return The hashcode.
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId(), getTitle(), getCardLists(), getTags());
    }

    /**
     * Creates a string of the instance.
     *
     * @return A string representing the instance.
     */
    @Override
    public String toString() {
        return "Board{" + "id=" + id + ", cardLists=" + cardLists + ", tags=" + getTags() + ", title='" + title + '\'' +
               '}';
    }

    private void forEachObserver(final Consumer<BoardObserver> observerConsumer) {
        if (this.observers != null) {
            this.observers.forEach(observerConsumer);
        }
    }

    /**
     * This class is a patcher for a board, which can perform various operations such as setting the title of the board,
     * adding or removing a CardList, moving a card list, patching a card list, and cross-list card move.
     * The class also has a method to patch the board with a given patcher.
     */
    public final class BoardPatcher {
        /**
         * Returns the board we are patching.
         *
         * @return The board.
         */
        public Board getBaseBoard() {
            return Board.this;
        }

        /**
         * setter for the title.
         *
         * @param newTitle the title to be set.
         */
        public void setTitle(final String newTitle) {
            Board.this.title = newTitle;

            Board.this.forEachObserver(observer -> observer.titleSet(newTitle));
        }

        /**
         * Removes a given Board from the board list, identified by id.
         */
        public void removeBoard() {
            if (Board.this.observers != null) {
                Board.this.observers.forEach(BoardObserver::boardRemoved);
            }
        }

        private CardList findCardList(final long cardListId) {
            return Board.this.cardListById(cardListId);
        }

        /**
         * Adds a new CardList to the Board.
         *
         * @param list The new list.
         */
        public void addCardList(final CardList list) {
            list.setBoard(Board.this.id);
            Board.this.cardLists.add(list);

            Board.this.forEachObserver(o -> o.listCreated(list));
        }

        /**
         * Removes a given CardList from the board, identified by id.
         *
         * @param cardListId The id of the CardList to remove from the board.
         */
        public void removeCardList(final long cardListId) {
            var list = findCardList(cardListId);

            if (!Board.this.cardLists.remove(list)) {
                throw new NoSuchElementException();
            }

            Board.this.forEachObserver(observer -> observer.listRemoved(list));
        }

        /**
         * Moves the card list identified by cardListId, such that it will
         * be positioned after the list identified by the placedAfterId.
         *
         * @param cardListId    The id of the card list to move.
         * @param placedAfterId The id of the card list to place this list after.
         *                      Or 0 if the list should be moved before all the other lists of the board.
         */
        public void moveCardList(final long cardListId, final long placedAfterId) {
            Predicate<CardList> placedAfterListPredicate = (list) -> list.getId() == placedAfterId;

            CardList card = findCardList(cardListId);
            Optional<CardList> placedAfter = Board.this.cardLists.stream().filter(placedAfterListPredicate).findFirst();

            Board.this.cardLists.remove(card);

            int insertIndex =
                    placedAfter.map(Board.this.cardLists::indexOf).map(it -> it + 1) // insert after, not before
                            .orElse(0);

            Board.this.cardLists.add(insertIndex, card);

            Board.this.forEachObserver(observer -> observer.listsReordered(card, placedAfter.orElse(null)));
        }

        /**
         * Patches the given card list.
         *
         * @param cardListId The card list id.
         * @param patcher    The patcher.
         */
        public void patchCardList(final long cardListId, final Consumer<CardList.CardListPatcher> patcher) {
            var cardList = findCardList(cardListId);

            cardList.patch(patcher);
        }

        /**
         * A cross-list card move.
         *
         * @param srcCardListId   The id of the source card list.
         * @param srcCardId       The id of the card to move.
         * @param destCardListId  The id of the destination card list.
         * @param destCardAfterId The id of the card that the moved card will be placed after, or 0 if the card should
         *                        be moved to the very beginning of the destination card list.
         * @param saveCard        Consumer that saves a card.
         * @param saveCardList    Consumer that saves a card list.
         */
        public void xListCardMove(final long srcCardListId, final long srcCardId, final long destCardListId,
                                  final long destCardAfterId, final Consumer<Card> saveCard,
                                  final Consumer<CardList> saveCardList) {
            if (srcCardListId == destCardListId) {
                throw new IllegalArgumentException("xlist move on the same list.");
            }
            var srcCardList = findCardList(srcCardListId);
            var destCardList = findCardList(destCardListId);

            var card = new Card[1];
            srcCardList.patch(sp -> card[0] = sp.removeCard(srcCardId, false));
            var hook =
                    destCardList.getCards().stream().filter(c -> c.getId() == destCardAfterId).findFirst().orElse(null);
            card[0].setCardList(destCardListId, true);

            destCardList.patch(sp -> sp.addCardAfter(card[0], hook));

            saveCardList.accept(srcCardList);
            saveCardList.accept(destCardList);
            saveCard.accept(card[0]);

            Board.this.forEachObserver(o -> o.xListCardMoved(srcCardList, card[0], destCardList, hook));
        }

        /**
         * Adds a new tag to the board.
         *
         * @param tag The new tag.
         */
        public void addTag(final Tag tag) {
            tag.setBoardId(Board.this.id);

            Board.this.tags.add(tag);

            Board.this.forEachObserver(o -> o.tagAdded(tag));
        }

        private Tag findTag(final long tagId) {
            return Board.this.tagById(tagId);
        }

        /**
         * Removes a tag from the board.
         *
         * @param tagId The id of the tag to delete.
         */
        public void removeTag(final long tagId) {
            var tag = findTag(tagId);

            Board.this.tags.remove(tag);

            Board.this.forEachObserver(o -> o.tagRemoved(tag));
        }

        /**
         * Patches a tag.
         *
         * @param tagId   The card id of the card to patch.
         * @param patcher The patcher.
         */
        public void patchTag(final long tagId, final Consumer<Tag.TagPatcher> patcher) {
            var tag = findTag(tagId);

            tag.patch(patcher);
        }

        /**
         * Sets the font color for the board.
         *
         * @param fontColor The new font color.
         */
        public void setFontColor(final String fontColor) {
            Board.this.fontColor = fontColor;

            Board.this.forEachObserver(o -> o.fontColorSet(fontColor));
        }

        /**
         * Sets the background color for the board.
         *
         * @param backgroundColor The new background color.
         */
        public void setBackgroundColor(final String backgroundColor) {
            Board.this.backgroundColor = backgroundColor;

            Board.this.forEachObserver(o -> o.backgroundColorSet(backgroundColor));
        }

        private ColorPreset findColorPreset(final long presetId) {
            return Board.this.colorPresetById(presetId);
        }

        /**
         * Sets the default card color preset for the board.
         *
         * @param presetId The preset id.
         */
        public void setDefaultCardColorPreset(final long presetId) {
            if (presetId == 0) {
                Board.this.defaultCardColorPreset = 0;
            } else {
                if (Board.this.presets.stream().noneMatch(it -> it.getId() == presetId)) {
                    throw new NoSuchColorPreset(presetId);
                }

                Board.this.defaultCardColorPreset = presetId;
            }

            Board.this.forEachObserver(o -> o.defaultCardColorPresetSet(Board.this.defaultCardColorPreset));
        }

        /**
         * Adds a new card color preset to the board.
         *
         * @param preset The preset.
         * @return The added color preset.
         */
        public ColorPreset addCardColorPreset(final ColorPreset preset) {
            var savedPreset = preset.clone();

            savedPreset.setBoard(Board.this.id);

            Board.this.presets.add(savedPreset);

            Board.this.forEachObserver(o -> o.colorPresetCreated(savedPreset));

            return savedPreset;
        }

        /**
         * Removes a preset from the list of presets.
         *
         * @param presetId The id of the preset to remove.
         * @return The removed preset.
         */
        public ColorPreset removeColorPreset(final long presetId) {
            var preset = findColorPreset(presetId);
            if (!Board.this.presets.remove(preset)) {
                throw new IllegalStateException();
            }

            Board.this.forEachObserver(o -> o.colorPresetRemoved(preset));

            return preset;
        }

        /**
         * Sets the name of the given preset.
         *
         * @param presetId The id of the preset.
         * @param name     The new name for the preset.
         */
        public void setPresetName(final long presetId, final String name) {
            var preset = findColorPreset(presetId);
            preset.setName(name);

            Board.this.forEachObserver(o -> o.colorPresetNameSet(presetId, name));
        }

        /**
         * Sets the font color for a given preset.
         *
         * @param presetId  The id of the preset.
         * @param fontColor The new font color.
         */
        public void setPresetFontColor(final long presetId, final String fontColor) {
            var preset = findColorPreset(presetId);
            preset.setForeground(fontColor);

            Board.this.forEachObserver(o -> o.colorPresetFontColorSet(presetId, fontColor));
        }

        /**
         * Sets the background color for the given preset.
         *
         * @param presetId        The id of the preset.
         * @param backgroundColor The new background color.
         */
        public void setPresetBackgroundColor(final long presetId, final String backgroundColor) {
            var preset = findColorPreset(presetId);
            preset.setBackground(backgroundColor);

            Board.this.forEachObserver(o -> o.colorPresetBackgroundColorSet(presetId, backgroundColor));
        }
    }

    /**
     * Gets the corresponding card list, by ID.
     *
     * @param cardListId The id of the card list to look for.
     * @return The card list.
     * @throws NoSuchCardList If no such card list was found.
     */
    public CardList cardListById(final long cardListId) throws NoSuchCardList {
        return this.cardLists.stream().filter(l -> l.getId() == cardListId).findFirst()
                .orElseThrow(() -> new NoSuchCardList(cardListId));
    }

    /**
     * Looks up the card by id.
     *
     * @param card The card id.
     * @return The card.
     * @throws NoSuchCard If no such card was found.
     */
    public Card cardById(final long card) throws NoSuchCard {
        return this.cardLists.stream().flatMap(it -> it.getCards().stream()).filter(it -> it.getId() == card)
                .findFirst().orElseThrow(() -> new NoSuchCard(card));
    }

    /**
     * Looks up the subtask within the board.
     *
     * @param subtask The subtask id.
     * @return The subtask.
     * @throws NoSuchSubtask If no such subtask exists.
     */
    public CardSubtask subtaskById(final long subtask) throws NoSuchSubtask {
        return this.cardLists.stream().flatMap(it -> it.getCards().stream()).flatMap(it -> it.getSubtasks().stream())
                .filter(it -> it.getId() == subtask).findFirst().orElseThrow(() -> new NoSuchSubtask(subtask));
    }

    /**
     * Looks up the color preset by id.
     *
     * @param colorPresetId The color preset id.
     * @return The color preset.
     * @throws NoSuchColorPreset If no such color preset was found.
     */
    public ColorPreset colorPresetById(final long colorPresetId) throws NoSuchColorPreset {
        return this.presets.stream().filter(it -> it.getId() == colorPresetId).findFirst()
                .orElseThrow(() -> new NoSuchColorPreset(colorPresetId));
    }

    /**
     * Looks up a tag by id.
     *
     * @param tagId The tag id.
     * @return The tag.
     * @throws NoSuchTag If no such tag exists.
     */
    public Tag tagById(final long tagId) throws NoSuchTag {
        return this.tags.stream().filter(it -> it.getId() == tagId).findFirst().orElseThrow(() -> new NoSuchTag(tagId));
    }

    /**
     * Patches the board with the given patcher.
     *
     * @param patcher The patcher.
     */
    public void patch(final Consumer<? super BoardPatcher> patcher) {
        var p = new BoardPatcher();

        patcher.accept(p);
    }
}
