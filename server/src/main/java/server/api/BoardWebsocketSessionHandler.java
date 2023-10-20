package server.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import commons.Board;
import commons.Card;
import commons.CardList;
import commons.CardSubtask;
import commons.ColorPreset;
import commons.Tag;
import commons.events.BoardBackgroundColorSetEvent;
import commons.events.BoardFontColorSetEvent;
import commons.events.BoardRemovedEvent;
import commons.events.BoardTitleSetEvent;
import commons.events.CardCategorySetEvent;
import commons.events.CardCreatedEvent;
import commons.events.CardDueDateSetEvent;
import commons.events.CardListTitleSetEvent;
import commons.events.CardMovedEvent;
import commons.events.CardPresetSetEvent;
import commons.events.CardRemovedEvent;
import commons.events.CardSubtaskCompletenessSetEvent;
import commons.events.CardSubtaskCreatedEvent;
import commons.events.CardSubtaskMovedEvent;
import commons.events.CardSubtaskNameSetEvent;
import commons.events.CardSubtaskRemovedEvent;
import commons.events.CardTagAddedEvent;
import commons.events.CardTagRemovedEvent;
import commons.events.CardTextSetEvent;
import commons.events.CardTitleSetEvent;
import commons.events.ClientToServerEvent;
import commons.events.ColorPresetBackgroundColorSetEvent;
import commons.events.ColorPresetCreatedEvent;
import commons.events.ColorPresetFontColorSetEvent;
import commons.events.ColorPresetNameSetEvent;
import commons.events.ColorPresetRemovedEvent;
import commons.events.DefaultCardColorPresetSetEvent;
import commons.events.ListBackgroundColorSetEvent;
import commons.events.ListCreatedEvent;
import commons.events.ListFontColorSetEvent;
import commons.events.ListRemovedEvent;
import commons.events.ListsReorderedEvent;
import commons.events.MessageProcessedEvent;
import commons.events.ServerToClientEvent;
import commons.events.SubscribeToBoard;
import commons.events.TagBackgroundColorSetEvent;
import commons.events.TagCreatedEvent;
import commons.events.TagDeletedEvent;
import commons.events.TagFontColorSetEvent;
import commons.events.TagNameSetEvent;
import commons.events.XListCardMoveEvent;
import commons.observers.BoardObserver;
import commons.observers.CardListObserver;
import commons.observers.CardObserver;
import commons.observers.SubtaskObserver;
import commons.observers.TagObserver;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import server.database.BoardRepository;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * This class represents a websocket session handler for a board. It handles incoming messages from clients and
 * sends updates to all connected clients about changes in the board through the MessageBroker.
 */
public class BoardWebsocketSessionHandler {
    private long boardId;
    private WebSocketSession session;
    private BoardRepository boardRepository;

    private final MessageBroker messageBroker;

    private WsBoardObserver observer;

    /**
     * Constructor.
     *
     * @param messageBroker The MessageBroker.
     */
    public BoardWebsocketSessionHandler(final MessageBroker messageBroker) {
        this.messageBroker = messageBroker;
    }

    /**
     * After connection established hook.
     *
     * @param session         The WebSocketSession.
     * @param boardRepository The BoardRepository.
     * @throws Exception Any exception.
     */
    public void afterConnectionEstablished(
            @NonNull final WebSocketSession session,
            @NonNull final BoardRepository boardRepository) throws Exception {
        this.session = session;
        this.boardRepository = boardRepository;
    }

    /**
     * Handles a text message received from the websocket.
     *
     * @param message The message.
     * @throws JsonProcessingException An exception that can occur.
     */
    protected void handleTextMessage(@NonNull final TextMessage message) throws JsonProcessingException {
        String payload = message.getPayload();
        this.send(new MessageProcessedEvent(payload));
        if (payload.toLowerCase().contains("verification")) {
            return;
        }

        ClientToServerEvent event = ClientToServerEvent.deserialize(payload);
        if (event instanceof final SubscribeToBoard e) {
            if (e.equals(SubscribeToBoard.UNSUBSCRIBE)) {
                this.handleUnsubscribeToBoard();
            } else {
                this.handleSubscribeToBoard(e);
            }
        } else {
            throw new RuntimeException("Unknown ClientToServer event");
        }
    }

    private void handleSubscribeToBoard(@NonNull final SubscribeToBoard subscribeToBoard) {
        if (observer != null) {
            messageBroker.removeBoardObserver(this.boardId, observer);
        }

        this.boardId = subscribeToBoard.getBoardId();

        observer = new WsBoardObserver();

        messageBroker.addBoardObserver(this.boardId, observer);
    }

    private void handleUnsubscribeToBoard() {
        if (this.observer != null) {
            this.messageBroker.removeBoardObserver(this.boardId, this.observer);
        }
    }

    /**
     * Invoke once the websocket connection closed.
     */
    protected void afterConnectionClosed() {
        messageBroker.removeBoardObserver(this.boardId, observer);
    }

    private void send(final ServerToClientEvent event) {
        try {
            var message = event.serialize();

            session.sendMessage(new TextMessage(message));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final class WsBoardObserver implements BoardObserver {
        private Board board;

        @Override
        public void setBoard(final Board board) {
            this.board = board;

            for (var list : board.getCardLists()) {
                var newListObserver = new WsCardListObserver();

                list.notify(newListObserver);
            }

            for (var tag : board.getTags()) {
                var newTagObserver = new WsTagObserver();
                tag.notify(newTagObserver);
            }
        }

        @Override
        public void listCreated(final CardList list) {
            var newObserver = new WsCardListObserver();

            list.notify(newObserver);

            BoardWebsocketSessionHandler.this.send(new ListCreatedEvent(list));
        }

        @Override
        public void listRemoved(final CardList list) {
            BoardWebsocketSessionHandler.this.send(new ListRemovedEvent(list));
        }

        @Override
        public void listsReordered(final CardList list, final CardList placedAfter) {
            BoardWebsocketSessionHandler.this.send(
                    new ListsReorderedEvent(list.getId(), placedAfter != null ? placedAfter.getId() : 0));
        }

        @Override
        public void xListCardMoved(final CardList srcList, final Card card, final CardList destList, final Card hook) {
            BoardWebsocketSessionHandler.this.send(new XListCardMoveEvent(
                    srcList.getId(), card.getId(), destList.getId(), hook != null ? hook.getId() : 0));
        }

        @Override
        public void titleSet(final String newTitle) {
            BoardWebsocketSessionHandler.this.send(new BoardTitleSetEvent(this.board.getId(), newTitle));
        }

        @Override
        public void tagAdded(final Tag tag) {
            BoardWebsocketSessionHandler.this.send(new TagCreatedEvent(tag));

            var newObserver = new WsTagObserver();
            tag.notify(newObserver);
        }

        @Override
        public void tagRemoved(final Tag tag) {
            BoardWebsocketSessionHandler.this.send(new TagDeletedEvent(tag));
        }

        @Override
        public void fontColorSet(final String fontColor) {
            BoardWebsocketSessionHandler.this.send(new BoardFontColorSetEvent(fontColor));
        }

        @Override
        public void backgroundColorSet(final String backgroundColor) {
            BoardWebsocketSessionHandler.this.send(new BoardBackgroundColorSetEvent(backgroundColor));
        }

        @Override
        public void colorPresetCreated(final ColorPreset preset) {
            BoardWebsocketSessionHandler.this.send(new ColorPresetCreatedEvent(preset));
        }

        @Override
        public void colorPresetRemoved(final ColorPreset preset) {
            BoardWebsocketSessionHandler.this.send(new ColorPresetRemovedEvent(preset));
        }

        @Override
        public void defaultCardColorPresetSet(final long colorPresetKey) {
            BoardWebsocketSessionHandler.this.send(new DefaultCardColorPresetSetEvent(colorPresetKey));
        }

        @Override
        public void colorPresetNameSet(final long presetKey, final String newName) {
            BoardWebsocketSessionHandler.this.send(new ColorPresetNameSetEvent(presetKey, newName));
        }

        @Override
        public void colorPresetFontColorSet(final long presetKey, final String newFontColor) {
            BoardWebsocketSessionHandler.this.send(new ColorPresetFontColorSetEvent(presetKey, newFontColor));
        }

        @Override
        public void colorPresetBackgroundColorSet(final long presetKey, final String newBackgroundColor) {
            BoardWebsocketSessionHandler.this.send(
                    new ColorPresetBackgroundColorSetEvent(presetKey, newBackgroundColor));
        }

        @Override
        public void boardRemoved() {
            BoardWebsocketSessionHandler.this.send(new BoardRemovedEvent());
            if (BoardWebsocketSessionHandler.this.observer != null) {
                BoardWebsocketSessionHandler.this.messageBroker.
                        removeBoardObserver(BoardWebsocketSessionHandler.this.boardId,
                                BoardWebsocketSessionHandler.this.observer);
            }

            BoardWebsocketSessionHandler.this.boardId = 0;
            BoardWebsocketSessionHandler.this.observer = null;
        }
    }

    private class WsCardListObserver implements CardListObserver {
        private CardList cardList;

        @Override
        public void setCardList(final CardList list) {
            this.cardList = list;

            for (var card : list.getCards()) {
                var newCardObserver = new WsCardObserver();

                card.notify(newCardObserver);
            }
        }

        @Override
        public void cardAdded(final Card newCard) {
            var newObserver = new WsCardObserver();

            newCard.notify(newObserver);

            BoardWebsocketSessionHandler.this.send(new CardCreatedEvent(this.cardList.getId(), newCard));
        }

        @Override
        public void cardRemoved(final Card card) {
            BoardWebsocketSessionHandler.this.send(new CardRemovedEvent(this.cardList.getId(), card));
        }

        @Override
        public void cardMoved(final Card card, final Card placedAfter) {
            BoardWebsocketSessionHandler.this.send(new CardMovedEvent(
                    this.cardList.getId(), card.getId(), placedAfter != null ? placedAfter.getId() : 0));
        }

        @Override
        public void titleSet(final String newTitle) {
            BoardWebsocketSessionHandler.this.send(new CardListTitleSetEvent(this.cardList.getId(), newTitle));
        }

        @Override
        public void fontColorSet(final String newFontColor) {
            BoardWebsocketSessionHandler.this.send(new ListFontColorSetEvent(this.cardList.getId(), newFontColor));
        }

        @Override
        public void backgroundColorSet(final String newBackgroundColor) {
            BoardWebsocketSessionHandler.this.send(
                    new ListBackgroundColorSetEvent(this.cardList.getId(), newBackgroundColor));
        }
    }

    private class WsCardObserver implements CardObserver {
        private Card card;

        @Override
        public void setCard(final Card card) {
            this.card = card;

            for (var subtask : card.getSubtasks()) {
                var newObserver = new WsSubtaskObserver();
                subtask.notify(newObserver);
            }
        }

        @Override
        public void titleSet(final String newTitle) {
            BoardWebsocketSessionHandler.this.send(new CardTitleSetEvent(card.getId(), newTitle));
        }

        @Override
        public void textSet(final String newText) {
            BoardWebsocketSessionHandler.this.send(new CardTextSetEvent(card.getId(), newText));
        }

        @Override
        public void categorySet(final String newCategory) {
            BoardWebsocketSessionHandler.this.send(new CardCategorySetEvent(card.getId(), newCategory));
        }

        @Override
        public void dueDateSet(final ZonedDateTime newDueDate) {
            BoardWebsocketSessionHandler.this.send(new CardDueDateSetEvent(card.getId(), newDueDate));
        }

        @Override
        public void presetSet(final long presetKey) {
            BoardWebsocketSessionHandler.this.send(new CardPresetSetEvent(card.getId(), presetKey));
        }

        @Override
        public void tagAdded(final Tag tag) {
            BoardWebsocketSessionHandler.this.send(new CardTagAddedEvent(card.getId(), tag.getId()));
        }

        @Override
        public void tagRemoved(final Tag tag) {
            BoardWebsocketSessionHandler.this.send(new CardTagRemovedEvent(card.getId(), tag.getId()));
        }

        @Override
        public void subtaskCreated(final CardSubtask subtask) {
            BoardWebsocketSessionHandler.this.send(new CardSubtaskCreatedEvent(card.getId(), subtask));

            var newObserver = new WsSubtaskObserver();
            subtask.notify(newObserver);
        }

        @Override
        public void subtaskDeleted(final CardSubtask subtask) {
            BoardWebsocketSessionHandler.this.send(new CardSubtaskRemovedEvent(card.getId(), subtask.getId()));
        }

        @Override
        public void subtaskMoved(final CardSubtask subtask, final CardSubtask placedAfter) {
            BoardWebsocketSessionHandler.this.send(new CardSubtaskMovedEvent(
                    this.card.getId(), subtask.getId(), placedAfter != null ? placedAfter.getId() : 0));
        }
    }

    private class WsTagObserver implements TagObserver {

        private Tag tag;

        @Override
        public void nameSet(final String newName) {
            BoardWebsocketSessionHandler.this.send(new TagNameSetEvent(tag, newName));
        }

        @Override
        public void setTag(final Tag tag) {
            this.tag = tag;
        }

        @Override
        public void fontColorSet(final String fontColor) {
            BoardWebsocketSessionHandler.this.send(new TagFontColorSetEvent(tag, fontColor));
        }

        @Override
        public void backgroundColorSet(final String backgroundColor) {
            BoardWebsocketSessionHandler.this.send(new TagBackgroundColorSetEvent(tag, backgroundColor));
        }
    }

    private final class WsSubtaskObserver implements SubtaskObserver {
        private CardSubtask subtask;

        @Override
        public void setSubtask(final CardSubtask subtask) {
            this.subtask = subtask;
        }

        @Override
        public void nameSet(final String newName) {
            BoardWebsocketSessionHandler.this.send(new CardSubtaskNameSetEvent(subtask.getId(), newName));
        }

        @Override
        public void completenessUpdated(final boolean newIsComplete) {
            BoardWebsocketSessionHandler.this.send(new CardSubtaskCompletenessSetEvent(subtask.getId(), newIsComplete));
        }
    }
}
