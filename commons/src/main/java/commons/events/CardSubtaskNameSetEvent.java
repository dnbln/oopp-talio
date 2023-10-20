package commons.events;

public record CardSubtaskNameSetEvent(long subtaskId, String newName) implements ServerToClientEvent {
}
