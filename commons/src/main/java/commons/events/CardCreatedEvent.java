package commons.events;

import commons.Card;

public record CardCreatedEvent(long cardListId, Card card) implements ServerToClientEvent {
}
