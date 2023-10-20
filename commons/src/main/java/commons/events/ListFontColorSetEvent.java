package commons.events;

public record ListFontColorSetEvent(long listId, String newFont) implements ServerToClientEvent {
}
