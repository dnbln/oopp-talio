package commons.events;

public record ColorPresetNameSetEvent(long presetKey, String name) implements ServerToClientEvent {
}
