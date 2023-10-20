package commons.events;

public record ListsReorderedEvent(long cardList, long placedAfter) implements ServerToClientEvent {
}
