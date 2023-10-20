package client.utils;

import client.scenes.MainCtrl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import commons.events.MessageProcessedEvent;
import commons.events.ServerToClientEvent;
import commons.events.SubscribeToBoard;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This is the websocket client endpoint. This class connect to the server with a websocket and handles updates.
 */
@ClientEndpoint
public class WebsocketClientEndpoint implements AutoCloseable {
    private static final long WEBSOCKET_WAIT_DURATION = 500L;
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String VERIFICATION_MESSAGE_TEMPLATE = "Verification message with id %d.";
    private long verificationId;
    private final MainCtrl mainCtrl;
    private Session session;
    private CountDownLatch countDownLatch;

    /**
     * Constructor for the WebsocketClientEndpoint. It is annotated with @Inject which means that this constructor is
     * used when the injector is asked to get an instance of this class.
     *
     * @param mainCtrl The MainCtrl to which the websocket event will be.
     */
    @Inject
    public WebsocketClientEndpoint(final MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    /**
     * Creates a new connection to the WebSocket.
     *
     * @param endpointURI The full URI of the WebSocket endpoint to connect to.
     * @throws DeploymentException  when connection to the websocket fails.
     * @throws IOException          when connection to the websocket fails.
     * @throws InterruptedException when the countdown latch is interrupted.
     */
    public void connect(final URI endpointURI) throws DeploymentException, IOException, InterruptedException {
        this.countDownLatch = new CountDownLatch(1);
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, endpointURI);
        if (!this.countDownLatch.await(WEBSOCKET_WAIT_DURATION, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException(
                    "WebSocket connection not established within %d milliseconds".formatted(WEBSOCKET_WAIT_DURATION));
        }
    }

    /**
     * Invoked when connection was established.
     *
     * @param ses The session.
     */
    @SuppressWarnings("unused")
    @OnOpen
    public void onOpen(final Session ses) {
        this.session = ses;
        this.verificationId = RandomUtils.RAND.nextLong(0, Long.MAX_VALUE);
        this.session.getAsyncRemote().sendText(VERIFICATION_MESSAGE_TEMPLATE.formatted(this.verificationId));
    }

    /**
     * Subscribe to a board.
     *
     * @param boardId the ID of the board you want to subscribe to.
     * @throws JsonProcessingException if serialization of the SubscribeToBoard instance fails.
     */
    public void subscribeTo(final long boardId) throws JsonProcessingException {
        this.session.getAsyncRemote().sendText(new SubscribeToBoard(boardId).serialize());
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
        ServerToClientEvent event = ServerToClientEvent.deserialize(message);
        LOGGER.info(message);

        if (event instanceof final MessageProcessedEvent eventMessage &&
            eventMessage.message().equals(VERIFICATION_MESSAGE_TEMPLATE.formatted(this.verificationId))) {
            this.countDownLatch.countDown();
            return;
        }
        this.mainCtrl.handleUpdate(event);
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
        this.session.close();
    }
}
