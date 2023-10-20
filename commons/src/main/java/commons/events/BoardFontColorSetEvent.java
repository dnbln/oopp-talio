package commons.events;

public record BoardFontColorSetEvent(String fontColor) implements ServerToClientEvent {
}
