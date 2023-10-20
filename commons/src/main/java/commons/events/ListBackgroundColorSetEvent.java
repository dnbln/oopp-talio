package commons.events;

public record ListBackgroundColorSetEvent(long listId, String newBackground) implements ServerToClientEvent {
}
