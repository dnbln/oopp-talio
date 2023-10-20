package server.api.exceptions;

import java.util.NoSuchElementException;

public class NoSuchBoard extends NoSuchElementException {
    private final long id;

    /**
     * Constructor.
     *
     * @param id Board id.
     */
    public NoSuchBoard(final long id) {
        super("No board with id " + id);
        this.id = id;
    }

    /**
     * Getter.
     * @return Board id.
     */
    public long getId() {
        return id;
    }
}
