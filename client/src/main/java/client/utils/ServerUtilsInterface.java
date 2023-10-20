package client.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import commons.Board;
import commons.Card;
import commons.CardList;
import commons.CardSubtask;
import commons.Tag;
import commons.events.ServerToClientEvent;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Consumer;

/**
 * The utility class that handles all interactions with the server.
 * <p>
 * This is an interface, so that testing becomes easier.
 * Whenever a class needs an object of this interface,
 * we can make our own version that does not actually connect
 * to the server, but checks whether the right methods have
 * been called.
 */
public interface ServerUtilsInterface extends AutoCloseable {
    /**
     * Get httpServerURI.
     *
     * @return httpServerURI The value to get.
     */
    URI getHttpServerURI();

    /**
     * Get wsServerURI.
     *
     * @return wsServerURI The value to get.
     */
    URI getWsServerURI();

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
    void validateAndSetServer(String url)
            throws InvalidServerException, InterruptedException, URISyntaxException, WebsocketConnectionException;

    /**
     * Initialized the websocket client endpoint and therefore creates a websocket connection to the client.
     * It waits one second before returning the client.
     *
     * @throws WebsocketConnectionException when connection to the websocket fails.
     * @throws InterruptedException         if the wait is interrupted.
     */
    void initWebsocket() throws WebsocketConnectionException, InterruptedException;

    /**
     * Subscribe to a new board.
     *
     * @param newBoardId the ID of the new board to subscribe to.
     * @throws JsonProcessingException if serialization of the SubscribeToBoard instance fails.
     */
    void subscribeToBoard(long newBoardId) throws JsonProcessingException;

    /**
     * Unsubscribe from a board.
     *
     * @throws JsonProcessingException if serialization of the UnsubscribeToBoard instance fails.
     */
    void unsubscribe() throws JsonProcessingException;

    /**
     * Get a list of all boards ordered by ID in ascending order.
     *
     * @return the list of boards.
     */
    List<Board> getBoards();

    /**
     * Get a list of specific boards.
     *
     * @param boardIds the IDs of the boards to get.
     * @return the list of boards.
     */
    List<Board> getBoards(List<Long> boardIds);

    /**
     * Get a board you are subscribed to from the server.
     *
     * @return the board you are subscribed to.
     */
    Board getBoard();

    /**
     * Get a board from the server.
     *
     * @param theBoardId the ID of the board you want to get.
     * @return the board you want to get.
     */
    Board getBoard(long theBoardId);

    /**
     * Add an empty board to the server.
     *
     * @return the board added. It contains the assigned ID.
     */
    Board addBoard();


    /**
     * Add a list to the board you are subscribed to.
     *
     * @return the list that was added. It now contains the correct ID as well.
     */
    CardList addList();

    /**
     * Get a list from the board you are subscribed to.
     *
     * @param listId the ID of the list to get
     * @return the list with the specified ID
     */
    CardList getList(long listId);

    /**
     * Set a new title for a list in the board you are subscribed to.
     *
     * @param listId   the ID of the list to change the title of
     * @param newTitle the new title of the list
     */
    void setListTitle(long listId, String newTitle);

    /**
     * Add a card to a list in the board you are subscribed to.
     *
     * @param listId the ID of the list the card will be added to.
     * @param card   the card that will be added. The card ID must be zero to prevent overwriting existing cards!
     * @return the card that was added. It now contains the assigned ID.
     */
    Card addCard(long listId, Card card);

    /**
     * Get a card from the board you are subscribed to.
     *
     * @param listId the ID of the list the card is contained in
     * @param cardId the ID of the card to get
     * @return the card with the specified ID in the list
     */
    Card getCard(long listId, long cardId);

    /**
     * Set a new title for a card in the board you are subscribed to.
     *
     * @param listId   the ID of the list the card is contained in
     * @param cardId   the ID of the card
     * @param newTitle the new title of the card
     */
    void setCardTitle(long listId, long cardId, String newTitle);

    /**
     * Set a new text description for a card in the board you are subscribed to.
     *
     * @param listId  the ID of the list the card is contained in
     * @param cardId  the ID of the card
     * @param newText the new text description of the card
     */
    void setCardText(long listId, long cardId, String newText);

    /**
     * Set a new due date for a card in the board you are subscribed to.
     *
     * @param listId     the ID of the list the card is contained in
     * @param cardId     the ID of the card
     * @param newDueDate the new due date of the card
     */
    void setCardDueDate(long listId, long cardId, String newDueDate);

    /**
     * Move a card after another, in the same list.
     *
     * @param listId      the ID of the list in which the cards are moved around.
     * @param cardId      the ID of the card that we wish to move
     * @param otherCardId the ID of the card that we wish to move after
     */
    void sameListCardMove(long listId, long cardId, long otherCardId);

    /**
     * Move a card across 2 lists.
     *
     * @param listId      the ID of the source list where the card is taken from
     * @param cardId      the ID of the card that we wish to move
     * @param otherListId the ID of the destination list where the card is moved in
     * @param otherCardId the ID of the card that we wish to move after, in the destination list
     */
    void xListCardMove(long listId, long cardId, long otherListId, long otherCardId);

    /**
     * Delete a board from the board list.
     *
     * @param theBoardId the ID of the board to be deleted
     */
    void deleteBoard(long theBoardId);

    /**
     * Delete a list from the board you are subscribed to.
     *
     * @param listId the ID of the list to be deleted
     */
    void deleteList(long listId);


    /**
     * Delete a card from the board you are subscribed to.
     *
     * @param listId the ID of the CardList in which the card is we delete.
     * @param cardId the ID of the card to be deleted.
     */
    void deleteCard(long listId, long cardId);

    /**
     * Set the tile of the board that has been selected.
     *
     * @param theBoardId the ID of the Board which we will change the title.
     * @param newTitle   the new title of the Board that has been selected.
     */
    void setBoardTitle(long theBoardId, String newTitle);

    /**
     * Registers for long-polling updates regarding the boards in the Board List view.
     * @param consumer the consumer which activates the refresh of the view
     */
    void registerForUpdates(Consumer<ServerToClientEvent> consumer);

    /**
     * Shuts down the currently running executor.
     */
    void stopLongPollingThread();

    /**
     * Get a list of all tags in a board.
     *
     * @return the list of tags.
     */
    List<Tag> getAllTags();

    /**
     * Creates a new tag for the board.
     *
     * @param tag the new tag.
     * @return the added tag.
     */
    Tag newTag(Tag tag);

    /**
     * Creates a new tag for the board.
     *
     * @param tagId the id of tag.
     * @return the desired tag.
     */
    Tag getTag(long tagId);

    /**
     * Add a tag for the specified card.
     *
     * @param listId id of the list.
     * @param cardId id of the card.
     * @param tag    the tag.
     */
    void addTag(long listId, long cardId, Tag tag);

    /**
     * Delete a tag from the board.
     *
     * @param tag the tag to delete.
     */
    void deleteTag(Tag tag);

    /**
     * Remove a tag from the specified card.
     *
     * @param listId id of the list.
     * @param cardId id of the card.
     * @param tag    the tag.
     */
    void removeTag(long listId, long cardId, Tag tag);

    /**
     * Add a subtask for the specified card.
     *
     * @param listId  id of the list.
     * @param cardId  id of the card.
     * @param subtask the subtask.
     * @return the new subtask.
     */
    CardSubtask newSubTask(long listId, long cardId, CardSubtask subtask);

    /**
     * Remove a subtask from the specified card.
     *
     * @param listId  id of the list.
     * @param cardId  id of the card.
     * @param subtask the subtask.
     */
    void removeSubTask(long listId, long cardId, CardSubtask subtask);

    /**
     * Move a subtask after another.
     *
     * @param listId      the ID of the list
     * @param cardId      the ID of the card
     * @param subtaskId   the ID of the subtask to move
     * @param otherSubtaskId the ID of the subtask that we wish to move after
     */
    void subtaskMove(long listId, long cardId, long subtaskId, long otherSubtaskId);

    /**
     * Set the completeness of a subtask.
     *
     * @param listId     id of the list.
     * @param cardId     id of the card.
     * @param subtask    the subtask.
     * @param isComplete the completeness of the subtask.
     */
    void setSubtaskCompleteness(long listId, long cardId, CardSubtask subtask, boolean isComplete);

    /**
     * Set a new color to the board background.
     *
     * @param newColor the new color of the board background.
     */
    void setBoardBackgroundColor(String newColor);

    /**
     * Set a new color to the board font.
     *
     * @param newColor the new color of the board font.
     */
    void setBoardFontColor(String newColor);

    /**
     * Set a new color to the card list background.
     *
     * @param listId  the ID of the list.
     * @param newColor the new color of the card list background.
     */
    void setCardListBackgroundColor(long listId, String newColor);

    /**
     * Set a new color to the card list font.
     *
     * @param listId  the ID of the board.
     * @param newColor the new color of the card list font.
     */
    void setCardListFontColor(long listId, String newColor);

    /**
     * Set a new color to the tag background.
     *
     * @param id  the ID of the tag.
     * @param backgroundColor the new color of the tag background.
     */
    void changeTagBackgroundColor(long id, String backgroundColor);

    /**
     * Set a new color to the tag background.
     *
     * @param id  the ID of the tag.
     * @param fontColor the new color of the tag font.
     */
    void changeTagFontColor(long id, String fontColor);
}
