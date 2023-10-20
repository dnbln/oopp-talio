package commons.events;

public record ColorPresetBackgroundColorSetEvent(long presetKey,
                                                 String background) implements ServerToClientEvent {
}
