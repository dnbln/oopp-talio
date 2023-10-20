package commons.patchExceptions;

import java.util.NoSuchElementException;

public class NoSuchSubtask extends NoSuchElementException {
    private final long subtaskId;

    /**
     * Constructor.
     *
     * @param subtaskId Subtask id.
     */
    public NoSuchSubtask(final long subtaskId) {
        super("No subtask with id " + subtaskId);
        this.subtaskId = subtaskId;
    }

    /**
     * Getter for subtask id.
     *
     * @return Subtask id.
     */
    public long getSubtaskId() {
        return subtaskId;
    }
}
