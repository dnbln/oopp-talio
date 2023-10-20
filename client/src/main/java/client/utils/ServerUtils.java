package client.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import commons.Board;
import commons.Card;
import commons.CardList;
import commons.CardSubtask;
import commons.Tag;
import commons.events.ServerToClientEvent;
import jakarta.websocket.DeploymentException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The utility class that handles all interactions with the server.
 */
public class ServerUtils implements ServerUtilsInterface, AutoCloseable {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int I_AM_A_TEAPOT = 418;
    private URI httpServerURI;
    private URI wsServerURI;
    private final WebsocketClientEndpoint wsClient;
    private final Client httpClient;
    private long boardId;

    /**
     * The constructor. Here the websocket client object gets injected.
     *
     * @param client provided by the injector. This client will be initialized when {@code validateAndSetServer()}
     *               or {@code initWebsocket()} is called.
     */
    @Inject
    public ServerUtils(final WebsocketClientEndpoint client) {
        this.wsClient = client;
        this.httpClient = ClientBuilder.newClient(new ClientConfig());
    }

    /**
     * Get httpServerURI.
     *
     * @return httpServerURI The value to get.
     */
    @Override
    public URI getHttpServerURI() {
        return this.httpServerURI;
    }

    /**
     * Get wsServerURI.
     *
     * @return wsServerURI The value to get.
     */
    @Override
    public URI getWsServerURI() {
        return this.wsServerURI;
    }

    /**
     * Sets the Server URI.
     *
     * @param url the URL to set it with.
     * @throws InvalidServerException       when a connection could not be made to the server.
     * @throws InterruptedException         when the thread is sleeping after connecting to the websocket, and the
     *                                      thread is interrupted.
     * @throws URISyntaxException           to indicate that the url could not be parsed as a URI reference.
     * @throws WebsocketConnectionException when connection to the websocket fails.
     */
    @Override
    public void validateAndSetServer(final String url)
            throws InvalidServerException, InterruptedException, URISyntaxException, WebsocketConnectionException {

        this.httpServerURI = new URI(url);
        this.wsServerURI = new URI("ws", this.httpServerURI.getUserInfo(), this.httpServerURI.getHost(),
                this.httpServerURI.getPort(), this.httpServerURI.getPath(), this.httpServerURI.getQuery(),
                this.httpServerURI.getFragment()).resolve("/board");

        WebTarget target = this.httpClient.target(this.httpServerURI);
        Response.StatusType res = target.request().get().getStatusInfo();
        if (!Objects.equals(I_AM_A_TEAPOT, res.getStatusCode())) {
            throw new InvalidServerException("Could connect, but the provided URL is not a Talio server.");
        }
        this.initWebsocket();
    }

    /**
     * Connects the websocket client endpoint to the server.
     * It waits one second before returning.
     *
     * @throws WebsocketConnectionException when connection to the websocket fails.
     * @throws InterruptedException         if the wait is interrupted.
     */
    @Override
    public void initWebsocket() throws WebsocketConnectionException, InterruptedException {
        try {
            this.wsClient.connect(this.wsServerURI);
        } catch (final DeploymentException | IOException e) {
            throw new WebsocketConnectionException(e);
        }
    }

    /**
     * Subscribe to a new board.
     *
     * @param newBoardId the ID of the new board to subscribe to.
     * @throws JsonProcessingException if serialization of the SubscribeToBoard instance fails.
     */
    @Override
    public void subscribeToBoard(final long newBoardId) throws JsonProcessingException {
        this.boardId = newBoardId;
        this.wsClient.subscribeTo(newBoardId);
    }

    /**
     * Unsubscribe from a board.
     *
     * @throws JsonProcessingException if serialization of the UnsubscribeToBoard instance fails.
     */
    @Override
    public void unsubscribe() throws JsonProcessingException {
        this.wsClient.unsubscribe();
    }

    /**
     * Get a list of all boards ordered by ID in ascending order.
     *
     * @return the list of boards.
     */
    @Override
    public List<Board> getBoards() {
        return this.httpClient.target(this.httpServerURI).path("/boards/all").request(MediaType.APPLICATION_JSON)
                .get(new BoardListGenericType());
    }

    /**
     * Get a list of specific boards.
     *
     * @param boardIds the IDs of the boards to get.
     * @return the list of boards.
     */
    public List<Board> getBoards(final List<Long> boardIds) {
        String queryParam = boardIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return this.httpClient.target(this.httpServerURI).path("/boards").queryParam("id", queryParam)
                .request(MediaType.APPLICATION_JSON)
                .get(new BoardListGenericType());
    }

    /**
     * Get a board you are subscribed to from the server.
     *
     * @return the board you are subscribed to.
     */
    @Override
    public Board getBoard() {
        return this.httpClient.target(this.httpServerURI).path("/boards/%d".formatted(this.boardId))
                .request(MediaType.APPLICATION_JSON).get(Board.class);
    }

    /**
     * Get a board from the server.
     *
     * @param theBoardId the ID of the board you want to get.
     * @return the board you want to get.
     */
    @Override
    public Board getBoard(final long theBoardId) {
        try (Response response = this.httpClient.target(this.httpServerURI).path("/boards/%d".formatted(theBoardId))
                .request(MediaType.APPLICATION_JSON).get()) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list does not exist");
            }
            return response.readEntity(Board.class);
        }
    }

    /**
     * Add an empty board to the server.
     */
    @Override
    public Board addBoard() {
        return this.httpClient.target(this.httpServerURI).path("/boards").request(MediaType.APPLICATION_JSON)
                .post(null, Board.class);
    }

    /**
     * Set the tile of the board that has been selected.
     *
     * @param theBoardId the ID of the Board which we will change the title.
     * @param newTitle   the new title of the Board that has been selected.
     */
    @Override
    public void setBoardTitle(final long theBoardId, final String newTitle) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/board_title".formatted(theBoardId)).request()
                .put(Entity.entity(newTitle, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list does not exist");
            }
        }
    }

    // a new thread to execute long polling on
    private ExecutorService exec;
    private boolean currentlyLongPolling;
    /**
     * Registers for long-polling updates regarding the boards in the Board List view.
     * @param consumer the consumer which activates the refresh of the view
     */
    @Override
    public void registerForUpdates(final Consumer<ServerToClientEvent> consumer) {
        exec = Executors.newSingleThreadExecutor();
        currentlyLongPolling = true;
        exec.submit(() -> {
            while (!Thread.interrupted() && currentlyLongPolling) {
                Response update = null;
                try {
                    update = this.httpClient.target(this.httpServerURI)
                            .path("/updates").request().get(Response.class);
                } catch (Exception e) {
                    LOGGER.error("Could not long poll for an update.", e);
                }
                if (update.getStatusInfo().toEnum() == Response.Status.NOT_MODIFIED) {
                    continue;
                }
                try {
                    ServerToClientEvent event = update.readEntity(ServerToClientEvent.class);
                    consumer.accept(event);
                } catch (ProcessingException e) {
                    LOGGER.error("Update could not be processed.", e);
                }
            }
        });
    }

    /**
     * Shuts down the currently running executor.
     */
    @Override
    public void stopLongPollingThread() {
        exec.shutdownNow();
        currentlyLongPolling = false;
    }

    /**
     * Creates a new tag for the board.
     *
     * @param tag the new tag.
     */
    @Override
    public Tag newTag(final Tag tag) {
        return this.httpClient.target(this.httpServerURI).path("/boards/%d/tags".formatted(this.boardId))
                .request(MediaType.APPLICATION_JSON).post(Entity.entity(tag, MediaType.APPLICATION_JSON), Tag.class);
    }

    /**
     * Creates a new tag for the board.
     *
     * @param tagId the id of tag.
     */
    @Override
    public Tag getTag(final long tagId) {
        return this.httpClient.target(this.httpServerURI).path("/boards/%d/tags/%d".formatted(this.boardId, tagId))
                .request(MediaType.APPLICATION_JSON).get(Tag.class);
    }

    /**
     * Add a list to the board you are subscribed to.
     *
     * @return the list that was added. It now contains the correct ID as well.
     */
    @Override
    public CardList addList() {
        return this.httpClient.target(this.httpServerURI).path("/boards/%d/lists".formatted(this.boardId))
                .request(MediaType.APPLICATION_JSON).post(null, CardList.class);
    }

    /**
     * Get a list from the board you are subscribed to.
     *
     * @param listId the ID of the list to get
     * @return the list with the specified ID
     */
    @Override
    public CardList getList(final long listId) {
        return this.httpClient.target(this.httpServerURI).path("/boards/%d/lists/%d".formatted(this.boardId, listId))
                .request(MediaType.APPLICATION_JSON).get(CardList.class);
    }

    /**
     * Set a new title for a list in the board you are subscribed to.
     *
     * @param listId   the ID of the list to change the title of
     * @param newTitle the new title of the list
     */
    @Override
    public void setListTitle(final long listId, final String newTitle) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/list_title".formatted(this.boardId, listId)).request()
                .put(Entity.entity(newTitle, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list does not exist");
            }
        }
    }

    /**
     * Add a card to a list in the board you are subscribed to.
     *
     * @param listId the ID of the list the card will be added to.
     * @param card   the card that will be added. The card ID must be zero to prevent overwriting existing cards!
     * @return the card that was added. It now contains the assigned ID.
     */
    @Override
    public Card addCard(final long listId, final Card card) {
        if (card.getId() != 0L) {
            throw new IllegalArgumentException("card id should be 0");
        }
        return this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards".formatted(this.boardId, listId)).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(card, MediaType.APPLICATION_JSON), Card.class);
    }

    /**
     * Get a card from the board you are subscribed to.
     *
     * @param listId the ID of the list the card is contained in
     * @param cardId the ID of the card to get
     * @return the card with the specified ID in the list
     */
    @Override
    public Card getCard(final long listId, final long cardId) {
        return this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d".formatted(this.boardId, listId, cardId))
                .request(MediaType.APPLICATION_JSON).get(Card.class);
    }

    /**
     * Set a new title for a card in the board you are subscribed to.
     *
     * @param listId   the ID of the list the card is contained in
     * @param cardId   the ID of the card
     * @param newTitle the new title of the card
     */
    @Override
    public void setCardTitle(final long listId, final long cardId, final String newTitle) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/card_title".formatted(this.boardId, listId, cardId)).request()
                .put(Entity.entity(newTitle, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list or card does not exist");
            }
        }
    }

    /**
     * Set a new text description for a card in the board you are subscribed to.
     *
     * @param listId  the ID of the list the card is contained in
     * @param cardId  the ID of the card
     * @param newText the new text description of the card
     */
    @Override
    public void setCardText(final long listId, final long cardId, final String newText) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/card_text".formatted(this.boardId, listId, cardId)).request()
                .put(Entity.entity(newText, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list or card does not exist");
            }
        }
    }

    /**
     * Set a new due date for a card in the board you are subscribed to.
     *
     * @param listId     the ID of the list the card is contained in
     * @param cardId     the ID of the card
     * @param newDueDate the new due date of the card
     */
    @Override
    public void setCardDueDate(final long listId, final long cardId, final String newDueDate) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/due_date".formatted(this.boardId, listId, cardId)).request()
                .put(Entity.entity(newDueDate, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list or card does not exist");
            }
        }
    }

    /**
     * Get a list of all tags in a board.
     *
     * @return the list of tags.
     */
    @Override
    public List<Tag> getAllTags() {
        return this.httpClient.target(this.httpServerURI).path("/boards/%d/tags".formatted(this.boardId))
                .request(MediaType.APPLICATION_JSON).get(new TagListGenericType());
    }

    /**
     * Add a tag for the specified card.
     *
     * @param listId id of the list.
     * @param cardId id of the card.
     * @param tag    the tag.
     */
    @Override
    public void addTag(final long listId, final long cardId, final Tag tag) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/tags/+%d".formatted(this.boardId, listId, cardId, tag.getId()))
                .request().put(Entity.entity(tag.getName(), MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list or card does not exist");
            }
        }
    }

    /**
     * Delete a tag from the board.
     *
     * @param tag the tag to delete.
     */
    @Override
    public void deleteTag(final Tag tag) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/tags/%d".formatted(this.boardId, tag.getId())).request().delete()) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The tag does not exist");
            }
        }
    }

    /**
     * Remove a tag from the specified card.
     *
     * @param listId id of the list.
     * @param cardId id of the card.
     * @param tag    the tag.
     */
    @Override
    public void removeTag(final long listId, final long cardId, final Tag tag) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/tags/-%d".formatted(this.boardId, listId, cardId, tag.getId()))
                .request().put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The tag does not exist");
            }
        }
    }

    /**
     * Add a subtask for the specified card.
     *
     * @param listId  id of the list.
     * @param cardId  id of the card.
     * @param subtask the subtask.
     */
    @Override
    public CardSubtask newSubTask(final long listId, final long cardId, final CardSubtask subtask) {
        return this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/subtasks".formatted(this.boardId, listId, cardId))
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(subtask, MediaType.APPLICATION_JSON), CardSubtask.class);

    }

    /**
     * Remove a subtask from the specified card.
     *
     * @param listId  id of the list.
     * @param cardId  id of the card.
     * @param subtask the subtask.
     */
    @Override
    public void removeSubTask(final long listId, final long cardId, final CardSubtask subtask) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/subtasks/%d".formatted(this.boardId, listId, cardId,
                        subtask.getId())).request().delete()) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The subtask does not exist");
            }
        }
    }

    /**
     * Move a subtask after another.
     *
     * @param listId      the ID of the list
     * @param cardId      the ID of the card
     * @param subtaskId   the ID of the subtask to move
     * @param otherSubtaskId the ID of the subtask that we wish to move after
     */
    @Override
    public void subtaskMove(final long listId, final long cardId, final long subtaskId, final long otherSubtaskId) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/subtasks/%d/moveAfter/%d"
                        .formatted(this.boardId, listId, cardId, subtaskId, otherSubtaskId))
                .request().put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list, card or subtask does not exist");
            }
        }
    }

    /**
     * Set the completeness of a subtask.
     *
     * @param listId     id of the list.
     * @param cardId     id of the card.
     * @param subtask    the subtask.
     * @param isComplete the completeness of the subtask.
     */
    @Override
    public void setSubtaskCompleteness(final long listId, final long cardId, final CardSubtask subtask,
                                       final boolean isComplete) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/subtasks/%d/completeness".formatted(this.boardId, listId, cardId,
                        subtask.getId())).request().put(Entity.entity(isComplete, MediaType.APPLICATION_JSON_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The subtask does not exist");
            }
        }
    }

    /**
     * Move a card after another, in the same list.
     *
     * @param listId      the ID of the list in which the cards are moved around.
     * @param cardId      the ID of the card that we wish to move
     * @param otherCardId the ID of the card that we wish to move after
     */
    @Override
    public void sameListCardMove(final long listId, final long cardId, final long otherCardId) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/moveAfter/%d".formatted(this.boardId, listId, cardId, otherCardId))
                .request().put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list or card does not exist");
            }
        }
    }

    /**
     * Move a card across 2 lists.
     *
     * @param listId      the ID of the source list where the card is taken from
     * @param cardId      the ID of the card that we wish to move
     * @param otherListId the ID of the destination list where the card is moved in
     * @param otherCardId the ID of the card that we wish to move after, in the destination list
     */
    @Override
    public void xListCardMove(final long listId, final long cardId, final long otherListId, final long otherCardId) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d/xListMoveAfter/%d/%d".formatted(this.boardId, listId, cardId,
                        otherListId, otherCardId)).request().put(Entity.entity("", MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list or card does not exist");
            }
        }
    }

    /**
     * Delete a board from the board list.
     *
     * @param theBoardId the ID of the board to be deleted
     */
    @Override
    public void deleteBoard(final long theBoardId) {
        try (Response response = this.httpClient.target(this.httpServerURI).path("/boards/%d".formatted(theBoardId))
                .request().delete()) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The board does not exist");
            }
        }
    }

    /**
     * Delete a list from the board you are subscribed to.
     *
     * @param listId the ID of the list to be deleted
     */
    @Override
    public void deleteList(final long listId) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d".formatted(this.boardId, listId)).request().delete()) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list does not exist");
            }
        }
    }

    /**
     * Delete a card from the board you are subscribed to.
     *
     * @param listId the ID of the CardList in which the card is we delete.
     * @param cardId the ID of the card to be deleted
     */
    @Override
    public void deleteCard(final long listId, final long cardId) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/cards/%d".formatted(this.boardId, listId, cardId)).request().delete()) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The card does not exist");
            }
        }
    }

    /**
     * Set a new color to the board background.
     *
     * @param newColor the new color of the board background.
     */
    @Override
    public void setBoardBackgroundColor(final String newColor) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/backgroundColor".formatted(this.boardId)).request()
                .put(Entity.entity(newColor, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The board does not exist");
            }
        }
    }

    /**
     * Set a new color to the board font.
     *
     * @param newColor the new color of the board font.
     */
    @Override
    public void setBoardFontColor(final String newColor) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/fontColor".formatted(this.boardId)).request()
                .put(Entity.entity(newColor, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The board does not exist");
            }
        }
    }

    /**
     * Set a new color to the card list background.
     *
     * @param listId  the ID of the list.
     * @param newColor the new color of the card list background.
     */
    @Override
    public void setCardListBackgroundColor(final long listId, final String newColor) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/backgroundColor".formatted(this.boardId, listId)).request()
                .put(Entity.entity(newColor, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list does not exist");
            }
        }
    }

    /**
     * Set a new color to the card list font.
     *
     * @param listId  the ID of the board.
     * @param newColor the new color of the card list font.
     */
    @Override
    public void setCardListFontColor(final long listId, final String newColor) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/lists/%d/fontColor".formatted(this.boardId, listId)).request()
                .put(Entity.entity(newColor, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The list does not exist");
            }
        }
    }

    /**
     * Set a new color to the tag background.
     *
     * @param id  the ID of the tag.
     * @param backgroundColor the new color of the tag background.
     */
    @Override
    public void changeTagBackgroundColor(final long id, final String backgroundColor) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/tags/%d/tag_background_color".formatted(this.boardId, id)).request()
                .put(Entity.entity(backgroundColor, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The tag does not exist");
            }
        }
    }

    /**
     * Set a new color to the tag background.
     *
     * @param id  the ID of the tag.
     * @param fontColor the new color of the tag font.
     */
    @Override
    public void changeTagFontColor(final long id, final String fontColor) {
        try (Response response = this.httpClient.target(this.httpServerURI)
                .path("/boards/%d/tags/%d/tag_color".formatted(this.boardId, id)).request()
                .put(Entity.entity(fontColor, MediaType.TEXT_PLAIN_TYPE))) {
            if (response.getStatusInfo().toEnum() == Response.Status.NOT_FOUND) {
                throw new NotFoundException("The tag does not exist");
            }
        }
    }

    /**
     * Closes this resource.
     */
    @Override
    public void close() throws Exception {
        this.httpClient.close();
        if (this.wsClient != null) {
            this.wsClient.close();
        }
    }

    private static class BoardListGenericType extends GenericType<List<Board>> {
        // This class is intentionally left empty, as it is only used to specify the type parameter of GenericType.
    }

    private static class TagListGenericType extends GenericType<List<Tag>> {
        // This class is intentionally left empty, as it is only used to specify the type parameter of GenericType.
    }
}
