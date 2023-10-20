package commons.events;

import commons.CardSubtask;

public record CardSubtaskCreatedEvent(long cardId, CardSubtask subtask) implements ServerToClientEvent {
}
