package commons.events;

public record BoardTitleSetEvent(long boardId, String newTitle) implements ServerToClientEvent {
}
