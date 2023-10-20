package commons.events;

public record CardSubtaskCompletenessSetEvent(
        long subtaskId,
        boolean newCompleteness) implements ServerToClientEvent {
}
