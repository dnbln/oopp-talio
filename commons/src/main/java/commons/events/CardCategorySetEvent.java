package commons.events;

public record CardCategorySetEvent(long cardId, String newCategory) implements ServerToClientEvent {
}
