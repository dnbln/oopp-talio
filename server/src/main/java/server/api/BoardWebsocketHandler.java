package server.api;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import server.database.BoardRepository;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * WebSocket handler for handling connections to a board.
 */
@Component
public class BoardWebsocketHandler extends TextWebSocketHandler {
    private final Map<WebSocketSession, BoardWebsocketSessionHandler> sessions = new IdentityHashMap<>();
    private final BoardRepository boardRepository;
    private final MessageBroker messageBroker;

    /**
     * Constructor.
     *
     * @param boardRepository The board repository.
     * @param messageBroker The MessageBroker.
     */
    public BoardWebsocketHandler(final BoardRepository boardRepository, final MessageBroker messageBroker) {
        this.boardRepository = boardRepository;
        this.messageBroker = messageBroker;
    }

    /**
     * Invoked after a connection is established.
     *
     * @param session The session.
     * @throws Exception Exceptions.
     */
    @Override
    public void afterConnectionEstablished(@NonNull final WebSocketSession session) throws Exception {
        var handler = new BoardWebsocketSessionHandler(this.messageBroker);
        this.sessions.put(session, handler);

        handler.afterConnectionEstablished(session, this.boardRepository);
    }

    /**
     * Handles a text message received from a websocket.
     *
     * @param session The websocket session.
     * @param message The message.
     * @throws Exception Any exception during handling.
     */
    @Override
    protected void handleTextMessage(
            @NonNull final WebSocketSession session,
            @NonNull final TextMessage message) throws Exception {
        this.sessions.get(session).handleTextMessage(message);
    }

    /**
     * Invoked after the connection closed.
     *
     * @param session The session.
     * @param status  The status.
     * @throws Exception Exceptions.
     */
    @Override
    public void afterConnectionClosed(
            @NonNull final WebSocketSession session,
            @NonNull final CloseStatus status) throws Exception {
        var sess = this.sessions.remove(session);

        sess.afterConnectionClosed();
    }
}
