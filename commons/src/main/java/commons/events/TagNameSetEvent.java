package commons.events;


import commons.Tag;

public record TagNameSetEvent(Tag tag, String newName) implements ServerToClientEvent {
}
