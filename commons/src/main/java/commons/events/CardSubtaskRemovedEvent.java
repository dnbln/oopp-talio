package commons.events;

public record CardSubtaskRemovedEvent(long cardId, long subtaskId) implements ServerToClientEvent {
}
