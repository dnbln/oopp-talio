package commons.events;

import commons.ColorPreset;

public record ColorPresetRemovedEvent(ColorPreset preset) implements ServerToClientEvent {
}
