package commons.events;

public record XListCardMoveEvent(
        long srcCardList,
        long card,
        long destCardList,
        long hook) implements ServerToClientEvent {
}
