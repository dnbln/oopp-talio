package commons;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import commons.observers.CardObserver;
import commons.patchExceptions.NoSuchSubtask;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Transient;
import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Entity
public final class Card implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // package-private setId used for tests
    void setId(final long newId) {
        this.id = newId;
    }

    @Column
    private String title;

    @Column
    private String text;

    @Column
    private String category;

    @Column
    private ZonedDateTime dueDate;

    @Column
    private long cardList;

    @ManyToMany
    private Set<Tag> tags;

    @Column
    private long colorPresetKey;

    @OneToMany(mappedBy = "card", fetch = FetchType.EAGER)
    @OrderColumn(name = "subtaskSequence")
    private List<CardSubtask> subtasks;

    @Transient
    @JsonIgnore
    private ArrayList<CardObserver> observers;

    void setCardList(final long cardList, final boolean xlistMove) {
        if (this.cardList != 0 && (this.cardList != cardList && !xlistMove)) {
            throw new IllegalStateException("setCardList called more than one time");
        }

        this.cardList = cardList;
    }

    /**
     * Used on the back-end, to set the cardList id field before saving.
     *
     * @param cardList The card list.
     */
    public void presaveForList(final CardList cardList) {
        setCardList(cardList.getId(), false);
    }

    /**
     * Constructor for the class. It makes all strings empty and sets the {@code dueDate} to {@code null}.
     */

    public Card() {
        this.title = "";
        this.text = "";
        this.category = "";
        this.dueDate = null;
        this.tags = new HashSet<>();
        this.subtasks = new ArrayList<>();
    }

    /**
     * Constructor for the class. It makes all strings except the title empty and sets the {@code dueDate} to
     * {@code null}.
     *
     * @param title The title of the card.
     */
    public Card(final String title) {
        this.title = title;
        this.text = "";
        this.category = "";
        this.dueDate = null;
        this.tags = new HashSet<>();
        this.subtasks = new ArrayList<>();
    }

    /**
     * Constructor for the class.
     *
     * @param title    The title of the card.
     * @param text     The text description of the card.
     * @param category The category of the card.
     * @param dueDate  The due date of the card.
     * @param tags     The tags.
     */
    public Card(
            final String title,
            final String text,
            final String category,
            final ZonedDateTime dueDate,
            final Set<Tag> tags) {
        this.title = title;
        this.text = text;
        this.category = category;
        this.dueDate = dueDate;
        this.tags = tags;
        this.subtasks = new ArrayList<>();
    }

    /**
     * Adds the given observer to the list of observers to notify
     * when changes to this Card happen.
     *
     * @param observer The observer.
     */
    public void notify(final CardObserver observer) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }

        this.observers.add(observer);

        observer.setCard(this);
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
     * Get title.
     *
     * @return title The value to get.
     */
    @JsonGetter("title")
    public String getTitle() {
        return title;
    }

    /**
     * Get text.
     *
     * @return text The value to get.
     */
    @JsonGetter("text")
    public String getText() {
        return text;
    }

    /**
     * Get category.
     *
     * @return category The value to get.
     */
    @JsonGetter("category")
    public String getCategory() {
        return category;
    }

    /**
     * Get dueDate.
     *
     * @return dueDate The value to get.
     */
    @JsonGetter("dueDate")
    public ZonedDateTime getDueDate() {
        return dueDate;
    }

    /**
     * Get cardList.
     *
     * @return cardList The value to get.
     */
    @JsonGetter("cardList")
    public long getCardList() {
        return this.cardList;
    }

    /**
     * Get tags.
     *
     * @return The list of tag ids.
     */
    @JsonGetter("tags")
    public Set<Tag> getTags() {
        return tags;
    }

    /**
     * Get subtasks.
     *
     * @return The list of subtasks.
     */
    @JsonGetter("subtasks")
    public List<CardSubtask> getSubtasks() {
        return subtasks;
    }

    /**
     * Getter for the color preset key.
     *
     * @return The color preset key.
     */
    @JsonGetter("colorPresetKey")
    public long getColorPresetKey() {
        return colorPresetKey;
    }

    /**
     * Patches the Card, with the given patcher.
     *
     * @param patcher The patcher that will modify the card.
     */
    public void patch(final Consumer<CardPatcher> patcher) {
        var p = new CardPatcher();

        patcher.accept(p);
    }

    /**
     * Looks up a subtask by its id.
     *
     * @param subtaskId The subtask id.
     * @return The subtask.
     * @throws NoSuchSubtask If no such subtask was found.
     */
    public CardSubtask subtaskById(final long subtaskId) throws NoSuchSubtask {
        return this.subtasks.stream()
                .filter(it -> it.getId() == subtaskId)
                .findFirst().orElseThrow(() -> new NoSuchSubtask(subtaskId));
    }

    public final class CardPatcher {
        /**
         * Gets the card we are patching.
         *
         * @return The card.
         */
        public Card getBaseCard() {
            return Card.this;
        }

        private CardSubtask findSubtask(final long subtaskId) {
            return Card.this.subtaskById(subtaskId);
        }

        /**
         * Sets the title of the card.
         *
         * @param newTitle The new title of the card.
         */
        public void setTitle(final String newTitle) {
            Card.this.title = newTitle;

            Card.this.forEachObserver(observer -> observer.titleSet(newTitle));
        }

        /**
         * Sets the new text of the card.
         *
         * @param newText The new text.
         */
        public void setText(final String newText) {
            Card.this.text = newText;

            Card.this.forEachObserver(observer -> observer.textSet(newText));
        }

        /**
         * Sets the new category of the card.
         *
         * @param newCategory The new category.
         */
        public void setCategory(final String newCategory) {
            Card.this.category = newCategory;

            Card.this.forEachObserver(observer -> observer.categorySet(newCategory));
        }

        /**
         * Sets the new due date for the card.
         *
         * @param newDueDate The new due date.
         */
        public void setDueDate(final ZonedDateTime newDueDate) {
            Card.this.dueDate = newDueDate;

            Card.this.forEachObserver(observer -> observer.dueDateSet(newDueDate));
        }

        /**
         * Sets a color preset for the card.
         *
         * @param newColorPresetKey The new color preset key.
         */
        public void setColorPreset(final long newColorPresetKey) {
            Card.this.colorPresetKey = newColorPresetKey;

            Card.this.forEachObserver(observer -> observer.presetSet(newColorPresetKey));
        }

        /**
         * Adds a tag to the card.
         *
         * @param tag The tag id.
         */
        public void addTag(final Tag tag) {
            if (tag.getId() <= 0) {
                throw new IllegalArgumentException();
            }

            Card.this.tags.add(tag);

            Card.this.forEachObserver(observer -> observer.tagAdded(tag));
        }

        /**
         * Removes a tag from the card.
         *
         * @param tag The tag id.
         */
        public void removeTag(final Tag tag) {
            if (tag.getId() <= 0) {
                throw new IllegalArgumentException();
            }

            Card.this.tags.remove(tag);

            Card.this.forEachObserver(observer -> observer.tagRemoved(tag));
        }

        /**
         * Adds a new subtask to the card.
         *
         * @param subtask The new subtask.
         */
        public void addSubtask(final CardSubtask subtask) {
            subtask.setCard(Card.this.id);
            Card.this.subtasks.add(subtask);

            Card.this.forEachObserver(o -> o.subtaskCreated(subtask));
        }

        /**
         * Deletes a subtask from the card.
         *
         * @param subtaskId The id of the subtask to delete.
         */
        public void deleteSubtask(final long subtaskId) {
            var subtask = Card.this.subtaskById(subtaskId);

            if (!Card.this.subtasks.remove(subtask)) {
                throw new IllegalStateException();
            }

            Card.this.forEachObserver(o -> o.subtaskDeleted(subtask));
        }

        /**
         * Moves the subtask identified by the subtask id, such that it is positioned after
         * the subtask identified by the placedAfterId.
         *
         * @param subtaskId        The id of the subtask to move.
         * @param placedAfterId     The id of the subtask that this subtask will be placed after.
         *                      Use 0 to move a subtask to the very beginning of the list.
         */
        public void moveSubtask(final long subtaskId, final long placedAfterId) {
            Predicate<CardSubtask> placedAfterSubtaskPredicate = (subtask) -> subtask.getId() == placedAfterId;

            CardSubtask subtask = findSubtask(subtaskId);
            Optional<CardSubtask> placedAfter = Card.this.subtasks.stream()
                    .filter(placedAfterSubtaskPredicate).findFirst();

            Card.this.subtasks.remove(subtask);

            int insertIndex = placedAfter.map(Card.this.subtasks::indexOf)
                    .map(it -> it + 1) // insert after, not before
                    .orElse(0);

            Card.this.subtasks.add(insertIndex, subtask);

            Card.this.forEachObserver(observer -> observer.subtaskMoved(subtask, placedAfter.orElse(null)));
        }

        /**
         * Patches the given subtask.
         *
         * @param subtaskId      The subtask id.
         * @param subtaskPatcher The patcher.
         */
        public void patchSubtask(final long subtaskId, final Consumer<CardSubtask.SubtaskPatcher> subtaskPatcher) {
            var subtask = Card.this.subtaskById(subtaskId);
            subtask.patch(subtaskPatcher);
        }
    }

    private void forEachObserver(final Consumer<CardObserver> observerConsumer) {
        if (this.observers != null) {
            this.observers.forEach(observerConsumer);
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
        return Objects.hash(getId(), getTitle(), getText(), getCategory(), getDueDate(), getTags());
    }

    /**
     * Creates a string of the instance.
     *
     * @return A string representing the instance.
     */
    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", category='" + category + '\'' +
                ", dueDate=" + dueDate +
                ", tags=" + tags +
                '}';
    }
}
