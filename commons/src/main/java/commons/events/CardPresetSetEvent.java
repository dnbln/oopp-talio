package commons.events;

public record CardPresetSetEvent(long cardId, long presetKey) implements ServerToClientEvent {
}
