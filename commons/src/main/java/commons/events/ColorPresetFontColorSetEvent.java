package commons.events;

public record ColorPresetFontColorSetEvent(long presetKey, String fontColor) implements ServerToClientEvent {
}
