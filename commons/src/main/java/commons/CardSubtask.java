package commons;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import commons.observers.SubtaskObserver;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Entity
public final class CardSubtask implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String name;

    @Column
    private boolean completed;

    @Column
    private long card;

    @Transient
    @JsonIgnore
    private List<SubtaskObserver> observers;

    void setCard(final long cardId) {
        if (this.card != 0 && this.card != cardId) {
            throw new IllegalStateException();
        }

        this.card = cardId;
    }

    /**
     * Backend specific method to set the card id.
     *
     * @param card The card.
     */
    public void presaveForCard(final Card card) {
        this.setCard(card.getId());
    }

    /**
     * Constructor.
     */
    public CardSubtask() {
        this.id = 0;
        this.name = "";
        this.completed = false;
    }

    /**
     * Notifies the given observer on any changes to the subtask.
     *
     * @param observer The observer to notify.
     */
    public void notify(final SubtaskObserver observer) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }

        this.observers.add(observer);
        observer.setSubtask(this);
    }

    /**
     * Getter for id.
     *
     * @return The id.
     */
    @JsonGetter("id")
    public long getId() {
        return id;
    }

    /**
     * Getter for the card id.
     *
     * @return The card id.
     */
    @JsonGetter("card")
    public long getCard() {
        return card;
    }

    /**
     * Getter for name.
     *
     * @return Name.
     */
    @JsonGetter("name")
    public String getName() {
        return name;
    }

    /**
     * Getter for completed?
     *
     * @return Whether the subtask was completed.
     */
    @JsonGetter("completed")
    public boolean isCompleted() {
        return completed;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardSubtask that = (CardSubtask) o;

        if (id != that.id) {
            return false;
        }
        if (completed != that.completed) {
            return false;
        }
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        result = 31 * result + (completed ? 1 : 0);
        return result;
    }

    /**
     * Patches the subtask with the patcher.
     *
     * @param patcher The patcher.
     */
    public void patch(final Consumer<SubtaskPatcher> patcher) {
        patcher.accept(new SubtaskPatcher());
    }

    private void forEachObserver(final Consumer<SubtaskObserver> observerConsumer) {
        if (this.observers == null) {
            return;
        }

        this.observers.forEach(observerConsumer);
    }

    public final class SubtaskPatcher {
        /**
         * Gets the subtask we are patching.
         *
         * @return The subtask.
         */
        public CardSubtask getBaseSubtask() {
            return CardSubtask.this;
        }

        /**
         * Sets the name of the subtask.
         *
         * @param newName The new name.
         */
        public void setName(final String newName) {
            CardSubtask.this.name = newName;

            CardSubtask.this.forEachObserver(o -> o.nameSet(newName));
        }

        /**
         * Sets the new completeness status of the subtask.
         *
         * @param newIsComplete The new completeness status.
         */
        public void setCompleteness(final boolean newIsComplete) {
            CardSubtask.this.completed = newIsComplete;

            CardSubtask.this.forEachObserver(o -> o.completenessUpdated(newIsComplete));
        }
    }
}
