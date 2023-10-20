package commons.patchExceptions;

import java.util.NoSuchElementException;

public class NoSuchTag extends NoSuchElementException {
    private final long tagId;

    /**
     * Constructor.
     *
     * @param tagId Tag id.
     */
    public NoSuchTag(final long tagId) {
        super("No tag with id " + tagId);
        this.tagId = tagId;
    }

    /**
     * Getter for tag id.
     *
     * @return Tag id.
     */
    public long getTagId() {
        return tagId;
    }
}
