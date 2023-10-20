package commons.events;

import commons.ColorPreset;

public record ColorPresetCreatedEvent(ColorPreset preset) implements ServerToClientEvent {
}
