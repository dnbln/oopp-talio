package commons.events;

import commons.Tag;

public record TagBackgroundColorSetEvent(Tag tag, String newBackgroundColor) implements ServerToClientEvent {
}
