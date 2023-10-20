package commons.events;

import commons.Tag;

public record TagCreatedEvent(Tag tag) implements ServerToClientEvent {
}
