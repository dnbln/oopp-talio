package commons.events;

import java.time.ZonedDateTime;

public record CardDueDateSetEvent(long cardId, ZonedDateTime dueDate) implements ServerToClientEvent {
}
