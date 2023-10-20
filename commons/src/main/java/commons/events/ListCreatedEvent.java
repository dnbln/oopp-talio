package commons.events;

import commons.CardList;

/**
 * Represents an event that is sent from the server to the client when a new card list is created.
 */
public record ListCreatedEvent(CardList list) implements ServerToClientEvent {
}
