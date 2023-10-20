package client.utils;

import java.io.Serial;

/**
 * Thrown when a websocket connection to a server fails.
 */
public class WebsocketConnectionException extends Exception {
    @Serial
    private static final long serialVersionUID = 5386139240005010L;

    /**
     * Constructs a new InvalidServerURL.
     */
    public WebsocketConnectionException() {
    }

    /**
     * Constructs a new InvalidServerURL with the specified message.
     *
     * @param message the detail message of the exception
     */
    public WebsocketConnectionException(final String message) {
        super(message);
    }

    /**
     * Constructs a new WebsocketConnectionException with the specified cause.
     *
     * @param cause the cause of the exception.
     */
    public WebsocketConnectionException(final Exception cause) {
        super(cause);
    }
}
