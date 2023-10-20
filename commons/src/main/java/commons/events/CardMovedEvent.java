package commons.events;

/**
 * Event representing the move of a single card within a single list.
 *
 * @param cardListId  The id of the card list where both of the cards belong.
 * @param card        The card that was moved.
 * @param placedAfter The card that the moved card was placed after,
 *                    or 0 if it was moved to the beginning of the CardList.
 */
public record CardMovedEvent(long cardListId, long card, long placedAfter) implements ServerToClientEvent {
}
