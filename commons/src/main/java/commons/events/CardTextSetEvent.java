package commons.events;

public record CardTextSetEvent(long cardId, String newText) implements ServerToClientEvent {
}
