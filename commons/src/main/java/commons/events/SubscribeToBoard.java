package commons.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A class for subscribing to a board on the client side and sending that information to the server.
 * Extends ClientToServerEvent abstract class and includes methods for getting board ID, equalizing objects, and
 * hashing.
 */
public class SubscribeToBoard extends ClientToServerEvent {
    /**
     * An event that unsubscribes the websocket from the board it was previously subscribed to.
     * There is no board with boardId = 0, so we won't receive any events.
     */
    public static final ClientToServerEvent UNSUBSCRIBE = new SubscribeToBoard(0);

    private final long boardId;

    /**
     * Constructor.
     *
     * @param boardId The board ID.
     */
    @JsonCreator
    public SubscribeToBoard(@JsonProperty("board") final long boardId) {
        this.boardId = boardId;
    }

    /**
     * Returns the board id.
     *
     * @return The board id.
     */
    @JsonGetter("board")
    public long getBoardId() {
        return this.boardId;
    }

    /**
     * Equals impl.
     *
     * @param obj Other object
     * @return Whether they are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        SubscribeToBoard other = (SubscribeToBoard) obj;

        return this.boardId == other.getBoardId();
    }

    /**
     * Hash code impl.
     *
     * @return Hash code.
     */
    @Override
    public int hashCode() {
        return (int) (this.boardId ^ (this.boardId >>> 32));
    }
}
