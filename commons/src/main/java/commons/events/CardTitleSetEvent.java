package commons.events;

public record CardTitleSetEvent(long cardId, String newTitle) implements ServerToClientEvent {
}
