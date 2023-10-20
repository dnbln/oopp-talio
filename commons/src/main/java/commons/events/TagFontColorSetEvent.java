package commons.events;

import commons.Tag;

public record TagFontColorSetEvent(Tag tag, String newFontColor) implements ServerToClientEvent {
}
