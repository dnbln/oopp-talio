package commons.events;

public record CardTagRemovedEvent(long cardId, long tagId) implements ServerToClientEvent {
}
