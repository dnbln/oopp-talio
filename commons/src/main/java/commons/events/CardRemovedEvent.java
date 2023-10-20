package commons.events;

import commons.Card;

public record CardRemovedEvent(long cardListId, Card card) implements ServerToClientEvent {
}
