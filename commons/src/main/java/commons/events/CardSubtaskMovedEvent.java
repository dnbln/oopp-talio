package commons.events;

/**
 * Event representing the move of a subtask within a card.
 *
 * @param cardId  The id of the card where both of the subtasks belong.
 * @param subtask        The subtask that was moved.
 * @param placedAfter   The subtask that the moved subtask was placed after,
 *                    or 0 if it was moved to the beginning of the list of subtasks.
 */
public record CardSubtaskMovedEvent(long cardId, long subtask, long placedAfter) implements ServerToClientEvent {
}
