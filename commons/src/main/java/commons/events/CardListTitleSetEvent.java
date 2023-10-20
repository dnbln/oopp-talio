package commons.events;

public record CardListTitleSetEvent(long cardListId, String newTitle) implements ServerToClientEvent {
}
