package commons.events;

public record BoardBackgroundColorSetEvent(String background) implements ServerToClientEvent {
}
