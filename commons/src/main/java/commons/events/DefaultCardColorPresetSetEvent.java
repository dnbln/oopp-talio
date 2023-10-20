package commons.events;

public record DefaultCardColorPresetSetEvent(long presetKey) implements ServerToClientEvent {
}
