package commons.events;

import commons.Tag;

public record TagDeletedEvent(Tag tag) implements ServerToClientEvent {
}
