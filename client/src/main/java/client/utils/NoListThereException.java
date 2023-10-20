package client.utils;

import java.io.Serial;

/**
 * Thrown when a connection to the server could be established, but the server is not a Talio server.
 */
public class NoListThereException extends Exception {

    @Serial
    private static final long serialVersionUID = 2780205575703597236L;

    /**
     * Constructs a new InvalidServerURL.
     */
    public NoListThereException() {
    }

    /**
     * Constructs a new InvalidServerURL with the specified message.
     *
     * @param message the detail message of the exception
     */
    public NoListThereException(final String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidServerURL with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public NoListThereException(final Exception cause) {
        super(cause);
    }
}
