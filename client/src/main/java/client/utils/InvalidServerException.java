package client.utils;

import java.io.Serial;

/**
 * Thrown when a connection to the server could be established, but the server is not a Talio server.
 */
public class InvalidServerException extends Exception {
    @Serial
    private static final long serialVersionUID = -2020736654834725157L;

    /**
     * Constructs a new InvalidServerURL.
     */
    public InvalidServerException() {
    }

    /**
     * Constructs a new InvalidServerURL with the specified message.
     *
     * @param message the detail message of the exception
     */
    public InvalidServerException(final String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidServerURL with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public InvalidServerException(final Exception cause) {
        super(cause);
    }
}
