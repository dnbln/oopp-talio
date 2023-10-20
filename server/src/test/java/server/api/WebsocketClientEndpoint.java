package server.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import commons.events.MessageProcessedEvent;
import commons.events.ServerToClientEvent;
import commons.events.SubscribeToBoard;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.io.IOException;
import java.net.URI;
import java.util.Deque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This is the websocket client endpoint. This class connect to the server with a websocket and handles updates.
 */
@ClientEndpoint
public class WebsocketClientEndpoint implements AutoCloseable {
    private static final long WEBSOCKET_WAIT_DURATION = 500L;
    private final Deque<? super ServerToClientEvent> receivedEvents;
    private Session session;
    private long boardId;
    private CountDownLatch initCountdownLatch;
    private CountDownLatch shutdownLatch;

    /**
     * The constructor.
     *
     * @param receivedEvents the Deque in which the received events will be saved.
     */
    public WebsocketClientEndpoint(final Deque<? super ServerToClientEvent> receivedEvents) {
        this.receivedEvents = receivedEvents;
    }

    /**
     * Creates a new connection to the WebSocket.
     *
     * @param endpointURI        The full URI of the WebSocket endpoint to connect to.
     * @param subscribeToBoardId The board ID to subscribe to.
     * @param expectedEvents     The number of events we expect to receive.
     * @throws DeploymentException  when connection to the websocket fails.
     * @throws IOException          when connection to the websocket fails.
     * @throws InterruptedException when the countdown latch is interrupted.
     */
    void connect(final URI endpointURI, final long subscribeToBoardId, final int expectedEvents)
            throws DeploymentException, IOException, InterruptedException {
        this.boardId = subscribeToBoardId;
        this.initCountdownLatch = new CountDownLatch(1);
        this.shutdownLatch = new CountDownLatch(expectedEvents);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, endpointURI);
        if (!this.initCountdownLatch.await(WEBSOCKET_WAIT_DURATION, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException(
                    "WebSocket connection not established within %d milliseconds".formatted(WEBSOCKET_WAIT_DURATION));
        }
    }

    /**
     * Invoked when connection was established.
     *
     * @param ses The session.
     * @throws IOException if subscribing to the board fails.
     */
    @SuppressWarnings("unused")
    @OnOpen
    public void onOpen(final Session ses) throws IOException {
        this.session = ses;
        this.subscribeTo(this.boardId);
    }

    /**
     * Subscribe to a board.
     *
     * @param newBoardId the ID of the board you want to subscribe to.
     * @throws IOException if serialization of the SubscribeToBoard instance fails.
     */
    public void subscribeTo(final long newBoardId) throws IOException {
        this.boardId = newBoardId;
        RemoteEndpoint.Async asyncRemote = this.session.getAsyncRemote();
        asyncRemote.sendText(new SubscribeToBoard(newBoardId).serialize());
    }

    /**
     * Unsubscribe from a board.
     *
     * @throws JsonProcessingException if serialization of the UnsubscribeToBoard instance fails.
     */
    public void unsubscribe() throws JsonProcessingException {
        this.session.getAsyncRemote().sendText(SubscribeToBoard.UNSUBSCRIBE.serialize());
    }

    /**
     * Invoked on message.
     *
     * @param message The message.
     * @throws JsonProcessingException JSON processing exceptions.
     */
    @SuppressWarnings("unused")
    @OnMessage
    public void onMessage(final String message) throws JsonProcessingException {
        System.out.println("<<< " + message);
        ServerToClientEvent event = ServerToClientEvent.deserialize(message);
        if (event instanceof MessageProcessedEvent) {
            this.initCountdownLatch.countDown();
            return;
        }
        this.receivedEvents.add(event);
        this.shutdownLatch.countDown();
    }

    /**
     * Closes the session.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        if (!this.shutdownLatch.await(WEBSOCKET_WAIT_DURATION, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException(
                    "WebSocket did not receive all events after %d milliseconds".formatted(WEBSOCKET_WAIT_DURATION));
        }

        this.session.close();
    }
}
