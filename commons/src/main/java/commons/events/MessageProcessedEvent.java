package commons.events;

/**
 * Represents an event that is sent from server to client after a message is processed.
 */
public record MessageProcessedEvent(String message) implements ServerToClientEvent {
}
