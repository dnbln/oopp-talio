package commons.events;

public record CardTagAddedEvent(long cardId, long tagId) implements ServerToClientEvent {
}
