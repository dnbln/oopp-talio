package commons.observers;

import commons.Card;
import commons.CardSubtask;
import commons.Tag;

import java.time.ZonedDateTime;

public interface CardObserver {
    /**
     * Sets the card that the observer tracks.
     *
     * @param card The card to track.
     */
    void setCard(Card card);

    /**
     * Announces to the observer that the title of the card has changed.
     *
     * @param newTitle The new title.
     */
    void titleSet(String newTitle);

    /**
     * Announces to the observer that the text of the card has changed.
     *
     * @param newText The new text.
     */
    void textSet(String newText);

    /**
     * Announces to the observer that the category of the card has changed.
     *
     * @param newCategory The new category.
     */
    void categorySet(String newCategory);

    /**
     * Announces to the observer that the due date for the card has changed.
     *
     * @param newDueDate The new due date.
     */
    void dueDateSet(ZonedDateTime newDueDate);

    /**
     * Announces to the observer that a preset was set for this card.
     *
     * @param presetKey The key of the preset.
     */
    void presetSet(long presetKey);

    /**
     * Announces to the observer that a tag was added to this card.
     *
     * @param tag The id of the tag.
     */
    void tagAdded(Tag tag);

    /**
     * Announces to the observer that a tag was removed from this card.
     *
     * @param tag The id of the tag.
     */
    void tagRemoved(Tag tag);

    /**
     * Announces to the observer that a subtask was created for this card.
     *
     * @param subtask The new subtask.
     */
    void subtaskCreated(CardSubtask subtask);

    /**
     * Announces to the observer that a subtask was deleted which belonged to this card.
     *
     * @param subtask The deleted subtask.
     */
    void subtaskDeleted(CardSubtask subtask);

    /**
     * Called when a subtask is moved within its list of subtasks.
     *
     * @param subtask        The subtask that was moved.
     * @param placedAfter   The subtask that the moved subtask was placed after,
     *                    or null if the subtask was moved to the first position in the list of subtasks.
     */
    void subtaskMoved(CardSubtask subtask, CardSubtask placedAfter);
}
