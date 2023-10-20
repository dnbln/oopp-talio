package client.utils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The workspace of the user.
 */
public class Workspace implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = -9025616939982270300L;

    private List<Long> boardIds = new ArrayList<>();

    /**
     * Get ids.
     *
     * @return ids The value to get.
     */
    public List<Long> getBoardIds() {
        return this.boardIds;
    }

    /**
     * Give ids a new value.
     *
     * @param boardIds The variable it will be set to.
     */
    public void setBoardIds(final List<Long> boardIds) {
        this.boardIds = boardIds;
    }

    /**
     * Add a new boardId.
     *
     * @param boardId The boardId to add.
     */
    public void addBoardId(final Long boardId) {
        this.boardIds.add(boardId);
    }

    /**
     * Remove a boardId.
     *
     * @param boardId The boardId to remove.
     */
    public void removeBoardId(final Long boardId) {
        this.boardIds.remove(boardId);
    }

    /**
     * Clears all boardIds.
     */
    public void clearBoardIds() {
        this.boardIds.clear();
    }

    /**
     * Removes all boardIds that are not present in the given list.
     *
     * @param presentBoardIds the list of boardIds that should be present.
     */
    public void cleanupBoardIds(final Collection<Long> presentBoardIds) {
        this.boardIds.retainAll(presentBoardIds);
    }

    @Serial
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    @Serial
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "client.utils.Workspace{boardIds=%s}".formatted(this.boardIds);
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }

        Workspace workspace = (Workspace) obj;

        return new EqualsBuilder().append(this.getBoardIds(), workspace.getBoardIds())
                .isEquals();
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(this.getBoardIds()).toHashCode();
    }
}
