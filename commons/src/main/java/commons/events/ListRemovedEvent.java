package commons.events;

import commons.CardList;

public record ListRemovedEvent(CardList cardList) implements ServerToClientEvent {
}
