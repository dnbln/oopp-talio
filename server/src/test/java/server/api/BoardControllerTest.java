package server.api;

import commons.Board;
import commons.Card;
import commons.CardList;
import commons.CardSubtask;
import commons.ColorPreset;
import commons.Tag;
import commons.events.BoardBackgroundColorSetEvent;
import commons.events.BoardFontColorSetEvent;
import commons.events.BoardTitleSetEvent;
import commons.events.CardCreatedEvent;
import commons.events.CardDueDateSetEvent;
import commons.events.CardListTitleSetEvent;
import commons.events.CardPresetSetEvent;
import commons.events.CardSubtaskCompletenessSetEvent;
import commons.events.CardSubtaskCreatedEvent;
import commons.events.CardSubtaskNameSetEvent;
import commons.events.CardSubtaskRemovedEvent;
import commons.events.CardTagAddedEvent;
import commons.events.CardTagRemovedEvent;
import commons.events.CardTextSetEvent;
import commons.events.CardTitleSetEvent;
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
import commons.events.ServerToClientEvent;
import commons.events.TagBackgroundColorSetEvent;
import commons.events.TagCreatedEvent;
import commons.events.TagDeletedEvent;
import commons.events.TagFontColorSetEvent;
import commons.events.TagNameSetEvent;
import commons.events.XListCardMoveEvent;
import jakarta.websocket.DeploymentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Stream;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
final class BoardControllerTest {
    @LocalServerPort
    private int port;

    private String root;

    @Autowired
    private TestRestTemplate restTemplate;

    static <T> void assertIsOk(final ResponseEntity<T> entity) {
        Assertions.assertFalse(entity.getStatusCode().isError(), entity.getStatusCode().toString());
    }

    static <T> T assertOkAndUnwrap(final ResponseEntity<T> entity) {
        assertIsOk(entity);

        return Objects.requireNonNull(entity.getBody());
    }

    @BeforeEach
    void setRoot() {
        this.restTemplate.getRestTemplate()
                .setUriTemplateHandler(new DefaultUriBuilderFactory("http://localhost:%d/{path}".formatted(this.port)));
        this.root = "http://localhost:%d".formatted(this.port);
    }

    Board createBoard() {
        return assertOkAndUnwrap(
                this.restTemplate.postForEntity("%s/boards".formatted(this.root), null, Board.class));
    }

    Board getBoard(final long id) {
        return assertOkAndUnwrap(
                this.restTemplate.getForEntity("%s/boards/%d".formatted(this.root, id), Board.class));
    }

    List<Board> getAllBoards() {
        return List.of(assertOkAndUnwrap(
                this.restTemplate.getForEntity("%s/boards/all".formatted(this.root), Board[].class)));
    }

    List<Board> getSpecificBoards(final Collection<Long> boardIds) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("%s/boards".formatted(this.root));
        builder.queryParam("id", boardIds);

        URI uri = builder.build().encode().toUri();
        return List.of(assertOkAndUnwrap(this.restTemplate.getForEntity(uri, Board[].class)));
    }

    CardList createCardList(final long boardId) {
        return assertOkAndUnwrap(
                this.restTemplate.postForEntity("%s/boards/%d/lists".formatted(this.root, boardId), null,
                        CardList.class));
    }

    String tagUrl(final long boardId, final long tagId) {
        return "%s/boards/%d/tags/%d".formatted(this.root, boardId, tagId);
    }

    ResponseEntity<Void> moveCardList(final long boardId, final long listId, final long otherListId) {
        return this.restTemplate.exchange(
                "%s%s".formatted(this.root, "/boards/%d/lists/%d/moveAfter/%d".formatted(boardId, listId, otherListId)),
                HttpMethod.PUT, null, Void.class);
    }

    String cardListUrl(final long boardId, final long cardListId) {
        return "%s/boards/%d/lists/%d".formatted(this.root, boardId, cardListId);
    }

    CardList getCardList(final long boardId, final long cardListId) {
        return assertOkAndUnwrap(
                this.restTemplate.getForEntity(this.cardListUrl(boardId, cardListId), CardList.class));
    }

    void setCardListTitle(final long boardId, final long cardListId, final String newTitle) {
        this.restTemplate.put("%s/list_title".formatted(this.cardListUrl(boardId, cardListId)), newTitle);
    }

    void removeCardList(final long boardId, final long cardListId) {
        this.restTemplate.delete(this.cardListUrl(boardId, cardListId));
    }

    Card createCard(final long boardId, final long cardListId) {
        return assertOkAndUnwrap(
                this.restTemplate.postForEntity("%s/boards/%d/lists/%d/cards".formatted(this.root, boardId, cardListId),
                        new Card(), Card.class));
    }

    String cardUrl(final long boardId, final long cardListId, final long cardId) {
        return "%s/boards/%d/lists/%d/cards/%d".formatted(this.root, boardId, cardListId, cardId);
    }

    Card getCard(final long boardId, final long cardListId, final long cardId) {
        return assertOkAndUnwrap(
                this.restTemplate.getForEntity(this.cardUrl(boardId, cardListId, cardId), Card.class));
    }

    void setCardTitle(final long boardId, final long cardListId, final long cardId, final String cardTitle) {
        this.restTemplate.put("%s/card_title".formatted(this.cardUrl(boardId, cardListId, cardId)), cardTitle);
    }

    void setCardText(final long boardId, final long cardListId, final long cardId, final String cardText) {
        this.restTemplate.put("%s/card_text".formatted(this.cardUrl(boardId, cardListId, cardId)), cardText);
    }

    void setCardDueDate(final long boardId, final long cardListId, final long cardId,
                        final ChronoZonedDateTime<LocalDate> dueDate) {
        this.restTemplate.put("%s/due_date".formatted(this.cardUrl(boardId, cardListId, cardId)),
                dueDate.format(DateTimeFormatter.ISO_DATE_TIME));
    }

    void setBoardTitle(final long boardId, final String newTitle) {
        this.restTemplate.put("%s/boards/%d/board_title".formatted(this.root, boardId), newTitle);
    }

    Tag createTag(final long boardId) {
        return assertOkAndUnwrap(
                this.restTemplate.postForEntity("%s/boards/%d/tags".formatted(this.root, boardId), new Tag(),
                        Tag.class));
    }

    Set<Tag> getAllTags(final long boardId) {
        return assertOkAndUnwrap(
                this.restTemplate.exchange("%s/boards/%d/tags".formatted(this.root, boardId), HttpMethod.GET, null,
                        new SetParameterizedTypeReference()));
    }

    Tag getTag(final long boardId, final long tagId) {
        return assertOkAndUnwrap(
                this.restTemplate.getForEntity(this.tagUrl(boardId, tagId), Tag.class));
    }

    void addTagToCard(final long boardId, final long cardListId, final long cardId, final long tagId) {
        assert tagId > 0;
        this.restTemplate.put("%s/tags/+%d".formatted(this.cardUrl(boardId, cardListId, cardId), tagId), null);
    }

    void removeTagFromCard(final long boardId, final long cardListId, final long cardId, final long tagId) {
        assert tagId > 0;
        this.restTemplate.put("%s/tags/-%d".formatted(this.cardUrl(boardId, cardListId, cardId), tagId), null);
    }

    void deleteTag(final long boardId, final long tagId) {
        this.restTemplate.delete(this.tagUrl(boardId, tagId));
    }

    void setTagName(final long boardId, final long tagId, final String newName) {
        assert tagId > 0;
        this.restTemplate.put("%s/tag_name".formatted(this.tagUrl(boardId, tagId)), newName);
    }

    void setTagFontColor(final long boardId, final long tagId, final String newFontColor) {
        assert tagId > 0;
        this.restTemplate.put("%s/tag_color".formatted(this.tagUrl(boardId, tagId)), this.nullStr(newFontColor));
    }

    void setTagBackgroundColor(final long boardId, final long tagId, final String newBackgroundColor) {
        assert tagId > 0;
        this.restTemplate.put("%s/tag_background_color".formatted(this.tagUrl(boardId, tagId)),
                this.nullStr(newBackgroundColor));
    }

    void sameListMoveCard(final long board, final long cardList, final long card, final long hookCard) {
        this.restTemplate.put("%s/moveAfter/%d".formatted(this.cardUrl(board, cardList, card), hookCard), null);
    }

    void xlistMoveCard(final long board, final long srcCardList, final long srcCard, final long destCardList,
                       final long destCardHook) {
        this.restTemplate.put(
                "%s/boards/%d/lists/%d/cards/%d/xListMoveAfter/%d/%d".formatted(this.root, board, srcCardList, srcCard,
                        destCardList, destCardHook), null);
    }

    void setBoardFontColor(final long board, final String newFontColor) {
        this.restTemplate.put("%s/boards/%d/fontColor".formatted(this.root, board), this.nullStr(newFontColor));
    }

    void setBoardBackgroundColor(final long board, final String newBackgroundColor) {
        this.restTemplate.put("%s/boards/%d/backgroundColor".formatted(this.root, board),
                this.nullStr(newBackgroundColor));
    }

    void setCardListFontColor(final long board, final long cardList, final String fontColor) {
        this.restTemplate.put("%s/fontColor".formatted(this.cardListUrl(board, cardList)), this.nullStr(fontColor));
    }

    void setCardListBackgroundColor(final long board, final long cardList, final String backgroundColor) {
        this.restTemplate.put("%s/backgroundColor".formatted(this.cardListUrl(board, cardList)),
                this.nullStr(backgroundColor));
    }

    ColorPreset createPreset(final long board, final ColorPreset newPreset) {
        return assertOkAndUnwrap(
                this.restTemplate.postForEntity("%s/boards/%d/presets".formatted(this.root, board), newPreset,
                        ColorPreset.class));
    }

    String presetUrl(final long boardId, final long presetKey) {
        return "%s/boards/%d/presets/%d".formatted(this.root, boardId, presetKey);
    }

    ColorPreset getPreset(final long boardId, final long presetKey) {
        return assertOkAndUnwrap(
                this.restTemplate.getForEntity(this.presetUrl(boardId, presetKey), ColorPreset.class));
    }

    void deletePreset(final long board, final long presetKey) {
        this.restTemplate.delete(this.presetUrl(board, presetKey));
    }

    void setPresetName(final long board, final long presetKey, final String newName) {
        this.restTemplate.put("%s/name".formatted(this.presetUrl(board, presetKey)), newName);
    }

    void setPresetFontColor(final long board, final long presetKey, final String newFont) {
        this.restTemplate.put("%s/fontColor".formatted(this.presetUrl(board, presetKey)), this.nullStr(newFont));
    }

    void setPresetBackground(final long board, final long presetKey, final String newBackground) {
        this.restTemplate.put(this.presetUrl(board, presetKey) + "/backgroundColor", this.nullStr(newBackground));
    }

    String nullStr(final String s) {
        return s == null ? "null" : s;
    }

    void setDefaultPreset(final long board, final long presetKey) {
        this.restTemplate.put("%s/boards/%d/defaultPreset".formatted(this.root, board), presetKey);
    }

    void setCardPreset(final long board, final long cardList, final long card, final long presetKey) {
        this.restTemplate.put("%s/preset".formatted(this.cardUrl(board, cardList, card)), presetKey);
    }

    void removeBoard(final Board board) {
        this.restTemplate.delete("%s/boards/%d".formatted(this.root, board.getId()));
    }

    CardSubtask createSubtask(final long board, final long cardList, final long card, final CardSubtask subtask) {
        return assertOkAndUnwrap(
                this.restTemplate.postForEntity("%s/subtasks".formatted(this.cardUrl(board, cardList, card)), subtask,
                        CardSubtask.class));
    }

    String subtaskUrl(final long board, final long cardList, final long card, final long subtask) {
        return "%s/subtasks/%d".formatted(this.cardUrl(board, cardList, card), subtask);
    }

    CardSubtask getSubtask(final long board, final long cardList, final long card, final long subtask) {
        return assertOkAndUnwrap(
                this.restTemplate.getForEntity(this.subtaskUrl(board, cardList, card, subtask), CardSubtask.class));
    }

    void deleteSubtask(final long board, final long cardList, final long card, final long subtask) {
        this.restTemplate.delete(this.subtaskUrl(board, cardList, card, subtask));
    }

    void setSubtaskName(final long board, final long cardList, final long card, final long subtask,
                        final String newName) {
        this.restTemplate.put("%s/name".formatted(this.subtaskUrl(board, cardList, card, subtask)), newName);
    }

    void setSubtaskCompleteness(final long board, final long cardList, final long card, final long subtask,
                                final boolean newCompleteness) {
        this.restTemplate.put("%s/completeness".formatted(this.subtaskUrl(board, cardList, card, subtask)),
                newCompleteness);
    }

    @Test
    void createBoardTest() {
        var board = this.createBoard();

        Assertions.assertNotEquals(0, board.getId());
        Assertions.assertTrue(board.getCardLists().isEmpty());

        var board2 = this.getBoard(board.getId());

        Assertions.assertEquals(board, board2);
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void getAllBoardsTest() {
        var board1 = this.createBoard();
        var board2 = this.createBoard();

        var boards = this.getAllBoards();

        Assertions.assertEquals(List.of(board1, board2), boards,
                "The boards returned by the server do not match the boards created");
    }

    @Test
    void getSpecificBoardsTest() {
        var board1 = this.createBoard();
        var board2 = this.createBoard();
        var board3 = this.createBoard();
        var ignored = this.createBoard();

        var boards = this.getSpecificBoards(List.of(board2.getId(), board3.getId()));

        Assertions.assertEquals(List.of(board2, board3), boards,
                "The boards returned by the server do not match the boards created");

        var boards2 = this.getSpecificBoards(List.of(board1.getId(), 0L));

        Assertions.assertEquals(List.of(board1), boards2,
                "The boards returned by the server do not match the boards created");
    }

    @Test
    void getSpecificBoardsTestEmpty() {
        var ignored1 = this.createBoard();
        var ignored2 = this.createBoard();

        var boards = this.getSpecificBoards(new ArrayList<>());

        Assertions.assertEquals(Collections.emptyList(), boards,
                "The boards returned should be empty");
    }

    @Test
    void removeBoardTest() {
        var board = this.createBoard();
        this.removeBoard(board);
        var b2 = this.restTemplate.getForEntity("%s/boards/%d".formatted(this.root, board.getId()), Board.class);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, b2.getStatusCode());
    }

    @Test
    void createBoardCardList() {
        var board = this.createBoard();

        var cardList = this.createCardList(board.getId());

        var board2 = this.getBoard(board.getId());

        Assertions.assertEquals(board.getId(), board2.getId());
        Assertions.assertNotEquals(board.getCardLists(), board2.getCardLists());
        Assertions.assertEquals(board.getCardLists(), new ArrayList<>());
        Assertions.assertEquals(board2.getCardLists(), new ArrayList<>(List.of(cardList)));
    }

    /**
     * This method tests the functionality of moving a card list within a board.
     * A new board is created and three card lists are added to it. The method then checks that the board's
     * card lists are not empty and asserts that the expected and retrieved card lists are equal.
     * The first card list is then moved to the third position on the board, and an expected list is created after
     * the move.
     * Finally, the method asserts that the expected list and the actual list after the move are equal.
     * If any assertion fails, an error message will be displayed.
     */
    @Test
    void listMoveTestSuccess() {
        // Create a new board and get its ID
        final Board board = this.createBoard();
        final long boardId = board.getId();

        // Create three card lists on the board using a stream of CardList objects
        final int nrOfLists = 3;
        final List<CardList> cardLists = Stream.generate(() -> this.createCardList(boardId)).limit(nrOfLists).toList();

        // Check that the board's card lists are not empty. Assert that the expected and retrieved card lists are equal
        final Board updatedBoard = this.getBoard(boardId);
        Assertions.assertFalse(updatedBoard.getCardLists().isEmpty(), "The board's card list is empty.");
        Assertions.assertAll(() -> Assertions.assertIterableEquals(cardLists, updatedBoard.getCardLists(),
                        "The expected and retrieved cardLists are not equal."),
                () -> Assertions.assertEquals(cardLists.get(0), this.getCardList(boardId, cardLists.get(0).getId()),
                        "First card list not properly added"),
                () -> Assertions.assertEquals(cardLists.get(1), this.getCardList(boardId, cardLists.get(1).getId()),
                        "Second card list not properly added"),
                () -> Assertions.assertEquals(cardLists.get(2), this.getCardList(boardId, cardLists.get(2).getId()),
                        "Third card list not properly added"));

        // Move the first card list to the third position
        ResponseEntity<Void> responseEntity =
                this.moveCardList(boardId, cardLists.get(0).getId(), cardLists.get(2).getId());

        assertIsOk(responseEntity);

        // Create an expected list after the move
        final List<CardList> expectedCardLists = List.of(cardLists.get(1), cardLists.get(2), cardLists.get(0));

        // Assert that the expected list and the actual list after the move are equal
        Assertions.assertIterableEquals(expectedCardLists, this.getBoard(boardId).getCardLists(),
                "The expected cardLists and actual cardLists after moving are not equal.");
    }

    /**
     * Tests if `moveCardList()` returns 404 NOT_FOUND for non-existent board ID.
     */
    @Test
    void listMoveTestWrongBoardId() {
        // Define an ID without creating a board. This way we know for sure it is non-existent.
        final long boardId = 123;

        // Move some values, doesn't matter which because the board doesn't exist.
        ResponseEntity<Void> responseEntity = this.moveCardList(boardId, 1, 2);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
                "Calling the `moveCardList` method with a non-existent board ID should return 404 NOT_FOUND.");
    }

    /**
     * Tests moving a card list with a non-existent list ID, verifying that a 404 NOT_FOUND status code is returned.
     */
    @Test
    void listMoveTestWrongListId() {
        // Create a new board and get its ID
        final Board board = this.createBoard();
        final long boardId = board.getId();

        // Move non-existent lists
        ResponseEntity<Void> responseEntity = this.moveCardList(boardId, 1, 2);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode(),
                "Calling the `moveCardList` method with non-existent list ID's should return 404 NOT_FOUND.");
    }

    @Test
    void createCard() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());

        var board2 = this.getBoard(board.getId());

        Assertions.assertFalse(board2.getCardLists().isEmpty());
        Assertions.assertFalse(board2.getCardLists().get(0).getCards().isEmpty());
        Assertions.assertEquals(card, board2.getCardLists().get(0).getCards().get(0));
    }

    @Test
    void setCardTitle() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());

        var newTitle = "New title";

        this.setCardTitle(board.getId(), cardList.getId(), card.getId(), newTitle);

        var card2 = this.getCard(board.getId(), cardList.getId(), card.getId());

        Assertions.assertNotEquals(card, card2);
        Assertions.assertEquals(newTitle, card2.getTitle());
    }

    @Test
    void setCardText() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());

        var newText = "New text";

        this.setCardText(board.getId(), cardList.getId(), card.getId(), newText);

        var card2 = this.getCard(board.getId(), cardList.getId(), card.getId());

        Assertions.assertNotEquals(card, card2);
        Assertions.assertEquals(newText, card2.getText());
    }

    @Test
    void setCardListTitle() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());

        var newTitle = "New title";

        this.setCardListTitle(board.getId(), cardList.getId(), newTitle);

        var cardList2 = this.getCardList(board.getId(), cardList.getId());

        Assertions.assertNotEquals(cardList, cardList2);
        Assertions.assertEquals(newTitle, cardList2.getTitle());
    }

    @Test
    void addTagToCard() {
        var b = this.createBoard();
        var cardList = this.createCardList(b.getId());
        var card = this.createCard(b.getId(), cardList.getId());
        var tag = this.createTag(b.getId());

        this.addTagToCard(b.getId(), cardList.getId(), card.getId(), tag.getId());

        var c2 = this.getCard(b.getId(), cardList.getId(), card.getId());

        Assertions.assertEquals(1, c2.getTags().size());
        Assertions.assertEquals(Set.of(tag), c2.getTags());
    }

    @Test
    void removeTagFromCard() {
        var b = this.createBoard();
        var cardList = this.createCardList(b.getId());
        var card = this.createCard(b.getId(), cardList.getId());
        var tag = this.createTag(b.getId());

        this.addTagToCard(b.getId(), cardList.getId(), card.getId(), tag.getId());

        var c2 = this.getCard(b.getId(), cardList.getId(), card.getId());

        Assertions.assertEquals(1, c2.getTags().size());
        Assertions.assertEquals(Set.of(tag), c2.getTags());

        this.removeTagFromCard(b.getId(), cardList.getId(), card.getId(), tag.getId());

        var c3 = this.getCard(b.getId(), cardList.getId(), card.getId());

        Assertions.assertTrue(c3.getTags().isEmpty());
    }

    Instant truncatedToSecondsInstant(final ZonedDateTime time) {
        return time.toInstant().truncatedTo(ChronoUnit.SECONDS);
    }

    void assertZDTEquals(final ZonedDateTime expected, final ZonedDateTime actual) {
        Assertions.assertEquals(this.truncatedToSecondsInstant(expected), this.truncatedToSecondsInstant(actual));
    }

    @Test
    void setCardDueDate() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());

        var newDueDate = ZonedDateTime.now();

        this.setCardDueDate(board.getId(), cardList.getId(), card.getId(), newDueDate);

        var card2 = this.getCard(board.getId(), cardList.getId(), card.getId());

        Assertions.assertNotEquals(card, card2);
        this.assertZDTEquals(newDueDate, card2.getDueDate());
    }

    @Test
    void deleteCardList() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        this.removeCardList(board.getId(), cardList.getId());
        var b2 = this.getBoard(board.getId());

        Assertions.assertTrue(b2.getCardLists().isEmpty());
    }

    @Test
    void testGetAllTags() {
        var board = this.createBoard();
        var tag1 = this.createTag(board.getId());
        var tag2 = this.createTag(board.getId());

        var tags = this.getAllTags(board.getId());

        Assertions.assertEquals(Set.of(tag1, tag2), tags);
    }

    @Test
    void createTag() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());
        var t2 = this.getTag(board.getId(), tag.getId());

        Assertions.assertEquals(tag.getId(), t2.getId());
    }

    @Test
    void deleteTag() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());
        this.deleteTag(board.getId(), tag.getId());
        var b2 = this.getBoard(board.getId());

        Assertions.assertTrue(b2.getTags().isEmpty());
    }

    @Test
    void deleteTagRemovesFromCards() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());

        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        this.addTagToCard(board.getId(), cardList.getId(), card.getId(), tag.getId());
        this.deleteTag(board.getId(), tag.getId());

        var card2 = this.getCard(board.getId(), cardList.getId(), card.getId());

        Assertions.assertEquals(Set.of(), card2.getTags());
    }

    @Test
    void setTagName() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());
        String newName = "New Name";
        this.setTagName(board.getId(), tag.getId(), newName);

        Assertions.assertEquals(newName, this.getTag(board.getId(), tag.getId()).getName());
    }

    @Test
    void setTagFontColor() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());
        String newFontColor = "aaaa";
        this.setTagFontColor(board.getId(), tag.getId(), newFontColor);

        Assertions.assertEquals(newFontColor, this.getTag(board.getId(), tag.getId()).getFontColor());
    }

    @Test
    void setTagFontColorNull() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());
        this.setTagFontColor(board.getId(), tag.getId(), null);

        Assertions.assertNull(this.getTag(board.getId(), tag.getId()).getFontColor());
    }

    @Test
    void setTagBackgroundColor() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());
        String newBackgroundColor = "aaaa";
        this.setTagBackgroundColor(board.getId(), tag.getId(), newBackgroundColor);

        Assertions.assertEquals(newBackgroundColor, this.getTag(board.getId(), tag.getId()).getBackgroundColor());
    }

    @Test
    void setTagBackgroundColorNull() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());
        this.setTagBackgroundColor(board.getId(), tag.getId(), null);

        Assertions.assertNull(this.getTag(board.getId(), tag.getId()).getBackgroundColor());
    }

    @Test
    void sameListCardMove() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var card1 = this.createCard(board.getId(), cardList.getId());
        var card2 = this.createCard(board.getId(), cardList.getId());
        var card3 = this.createCard(board.getId(), cardList.getId());

        this.sameListMoveCard(board.getId(), cardList.getId(), card1.getId(), card2.getId());
        var cl = this.getCardList(board.getId(), cardList.getId());

        Assertions.assertEquals(new ArrayList<>(List.of(card2, card1, card3)), cl.getCards());
    }

    @Test
    void xlistMoveWorks() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var cardList2 = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        this.xlistMoveCard(board.getId(), cardList.getId(), card.getId(), cardList2.getId(), 0);
        var b2 = this.getBoard(board.getId());

        Assertions.assertEquals(2, b2.getCardLists().size());
        Assertions.assertTrue(b2.getCardLists().get(0).getCards().isEmpty());
        Assertions.assertEquals(1, b2.getCardLists().get(1).getCards().size());
        Assertions.assertEquals(card.getId(), b2.getCardLists().get(1).getCards().get(0).getId());
    }

    @Test
    void xlistMoveWithHookWorks() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var cardList2 = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        var card2 = this.createCard(board.getId(), cardList2.getId());
        this.xlistMoveCard(board.getId(), cardList.getId(), card.getId(), cardList2.getId(), card2.getId());
        var b2 = this.getBoard(board.getId());

        Assertions.assertEquals(2, b2.getCardLists().size());
        Assertions.assertTrue(b2.getCardLists().get(0).getCards().isEmpty());
        Assertions.assertEquals(2, b2.getCardLists().get(1).getCards().size());
        Assertions.assertEquals(card2.getId(), b2.getCardLists().get(1).getCards().get(0).getId());
        Assertions.assertEquals(card.getId(), b2.getCardLists().get(1).getCards().get(1).getId());
    }

    @Test
    void setBoardFontColor() {
        var board = this.createBoard();

        String newFontColor = "aaa";
        this.setBoardFontColor(board.getId(), newFontColor);

        var b2 = this.getBoard(board.getId());

        Assertions.assertEquals(newFontColor, b2.getFontColor());
    }

    @Test
    void setBoardFontColorToNull() {
        var board = this.createBoard();

        this.setBoardFontColor(board.getId(), null);

        var b2 = this.getBoard(board.getId());

        Assertions.assertNull(b2.getFontColor());
    }

    @Test
    void setBoardBackgroundColor() {
        var board = this.createBoard();

        String newBackgroundColor = "aaa";
        this.setBoardBackgroundColor(board.getId(), newBackgroundColor);

        var b2 = this.getBoard(board.getId());

        Assertions.assertEquals(newBackgroundColor, b2.getBackgroundColor());
    }

    @Test
    void setBoardBackgroundColorToNull() {
        var board = this.createBoard();

        this.setBoardBackgroundColor(board.getId(), null);

        var b2 = this.getBoard(board.getId());

        Assertions.assertNull(b2.getBackgroundColor());
    }

    @Test
    void setCardListFontColor() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());

        String newFontColor = "aaaa";

        this.setCardListFontColor(board.getId(), cardList.getId(), newFontColor);

        var cardList2 = this.getCardList(board.getId(), cardList.getId());

        Assertions.assertEquals(newFontColor, cardList2.getFontColor());
    }

    @Test
    void setCardListFontColorToNull() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());

        this.setCardListFontColor(board.getId(), cardList.getId(), null);

        var cardList2 = this.getCardList(board.getId(), cardList.getId());

        Assertions.assertNull(cardList2.getFontColor());
    }

    @Test
    void setCardListBackgroundColor() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());

        String newBackgroundColor = "aaaa";

        this.setCardListBackgroundColor(board.getId(), cardList.getId(), newBackgroundColor);

        var cardList2 = this.getCardList(board.getId(), cardList.getId());

        Assertions.assertEquals(newBackgroundColor, cardList2.getBackgroundColor());
    }

    @Test
    void setCardListBackgroundColorToNull() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());

        this.setCardListBackgroundColor(board.getId(), cardList.getId(), null);

        var cardList2 = this.getCardList(board.getId(), cardList.getId());

        Assertions.assertNull(cardList2.getBackgroundColor());
    }

    @Test
    void createPreset() {
        var b = this.createBoard();
        var preset = this.createPreset(b.getId(), new ColorPreset());

        var b2 = this.getBoard(b.getId());
        Assertions.assertEquals(new ArrayList<>(List.of(preset)), b2.getPresets());
    }

    @Test
    void deletePreset() {
        var b = this.createBoard();
        var preset = this.createPreset(b.getId(), new ColorPreset());

        var b2 = this.getBoard(b.getId());
        Assertions.assertEquals(new ArrayList<>(List.of(preset)), b2.getPresets());

        this.deletePreset(b.getId(), preset.getId());
        var b3 = this.getBoard(b.getId());
        Assertions.assertTrue(b3.getPresets().isEmpty());
    }

    @Test
    void setPresetName() {
        var b = this.createBoard();
        var preset = this.createPreset(b.getId(), new ColorPreset());
        String newName = "aaa";
        this.setPresetName(b.getId(), preset.getId(), newName);

        var p2 = this.getPreset(b.getId(), preset.getId());

        Assertions.assertEquals(newName, p2.getName());
    }

    @Test
    void setPresetFontColor() {
        var b = this.createBoard();
        var preset = this.createPreset(b.getId(), new ColorPreset());
        String newFontColor = "aaa";
        this.setPresetFontColor(b.getId(), preset.getId(), newFontColor);

        var p2 = this.getPreset(b.getId(), preset.getId());

        Assertions.assertEquals(newFontColor, p2.getForeground());
    }

    @Test
    void setPresetFontColorToNull() {
        var b = this.createBoard();
        var preset = this.createPreset(b.getId(), new ColorPreset());
        this.setPresetFontColor(b.getId(), preset.getId(), null);

        var p2 = this.getPreset(b.getId(), preset.getId());

        Assertions.assertNull(p2.getForeground());
    }

    @Test
    void setPresetBackgroundColor() {
        var b = this.createBoard();
        var preset = this.createPreset(b.getId(), new ColorPreset());
        String newBackgroundColor = "aaa";
        this.setPresetBackground(b.getId(), preset.getId(), newBackgroundColor);

        var p2 = this.getPreset(b.getId(), preset.getId());

        Assertions.assertEquals(newBackgroundColor, p2.getBackground());
    }

    @Test
    void setPresetBackgroundColorToNull() {
        var b = this.createBoard();
        var preset = this.createPreset(b.getId(), new ColorPreset());
        this.setPresetBackground(b.getId(), preset.getId(), null);

        var p2 = this.getPreset(b.getId(), preset.getId());

        Assertions.assertNull(p2.getBackground());
    }

    @Test
    void setDefaultPreset() {
        var b = this.createBoard();
        var preset = this.createPreset(b.getId(), new ColorPreset());
        this.setDefaultPreset(b.getId(), preset.getId());

        var b2 = this.getBoard(b.getId());

        Assertions.assertEquals(preset.getId(), b2.getDefaultCardColorPreset());
    }

    @Test
    void setCardPreset() {
        var b = this.createBoard();
        var preset = this.createPreset(b.getId(), new ColorPreset());
        var cardList = this.createCardList(b.getId());
        var card = this.createCard(b.getId(), cardList.getId());
        this.setCardPreset(b.getId(), cardList.getId(), card.getId(), preset.getId());
        var c2 = this.getCard(b.getId(), cardList.getId(), card.getId());
        Assertions.assertEquals(preset.getId(), c2.getColorPresetKey());
    }

    @Test
    void deletePresetRemovesFromCards() {
        var b = this.createBoard();
        var preset = this.createPreset(b.getId(), new ColorPreset());
        var cardList = this.createCardList(b.getId());
        var card = this.createCard(b.getId(), cardList.getId());
        this.setCardPreset(b.getId(), cardList.getId(), card.getId(), preset.getId());
        var c2 = this.getCard(b.getId(), cardList.getId(), card.getId());
        Assertions.assertEquals(preset.getId(), c2.getColorPresetKey());

        this.deletePreset(b.getId(), preset.getId());

        var c3 = this.getCard(b.getId(), cardList.getId(), card.getId());
        Assertions.assertEquals(0, c3.getColorPresetKey());
    }

    @Test
    void createSubtask() {
        var b = this.createBoard();
        var cardList = this.createCardList(b.getId());
        var card = this.createCard(b.getId(), cardList.getId());
        var subtask = this.createSubtask(b.getId(), cardList.getId(), card.getId(), new CardSubtask());
        var subtask2 = this.getSubtask(b.getId(), cardList.getId(), card.getId(), subtask.getId());

        Assertions.assertEquals(subtask.getId(), subtask2.getId());
        Assertions.assertEquals(card.getId(), subtask.getCard());
    }

    @Test
    void deleteSubtask() {
        var b = this.createBoard();
        var cardList = this.createCardList(b.getId());
        var card = this.createCard(b.getId(), cardList.getId());
        var subtask = this.createSubtask(b.getId(), cardList.getId(), card.getId(), new CardSubtask());
        this.deleteSubtask(b.getId(), cardList.getId(), card.getId(), subtask.getId());
        var card2 = this.getCard(b.getId(), cardList.getId(), card.getId());
        Assertions.assertTrue(card2.getSubtasks().isEmpty());
    }

    @Test
    void setSubtaskName() {
        var b = this.createBoard();
        var cardList = this.createCardList(b.getId());
        var card = this.createCard(b.getId(), cardList.getId());
        var subtask = this.createSubtask(b.getId(), cardList.getId(), card.getId(), new CardSubtask());
        String newName = "aaa";
        this.setSubtaskName(b.getId(), cardList.getId(), card.getId(), subtask.getId(), newName);
        var subtask2 = this.getSubtask(b.getId(), cardList.getId(), card.getId(), subtask.getId());

        Assertions.assertEquals(subtask.getId(), subtask2.getId());
        Assertions.assertEquals(newName, subtask2.getName());
    }

    @Test
    void setSubtaskCompleteness() {
        var b = this.createBoard();
        var cardList = this.createCardList(b.getId());
        var card = this.createCard(b.getId(), cardList.getId());
        var subtask = this.createSubtask(b.getId(), cardList.getId(), card.getId(), new CardSubtask());
        boolean newCompleteness = true;
        this.setSubtaskCompleteness(b.getId(), cardList.getId(), card.getId(), subtask.getId(), newCompleteness);
        var subtask2 = this.getSubtask(b.getId(), cardList.getId(), card.getId(), subtask.getId());

        Assertions.assertEquals(subtask.getId(), subtask2.getId());
        Assertions.assertEquals(newCompleteness, subtask2.isCompleted());
    }


    // =======================
    // Websocket tests below
    // =======================
    private final ConcurrentLinkedDeque<ServerToClientEvent> receivedEvents = new ConcurrentLinkedDeque<>();

    WebsocketClientEndpoint initWebsocket(final long boardId, final int expectedEvents) throws IOException {
        WebsocketClientEndpoint client;
        try {
            client = new WebsocketClientEndpoint(this.receivedEvents);
            client.connect(URI.create("ws://localhost:" + this.port + "/board"), boardId, expectedEvents);
        } catch (final DeploymentException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return client;
    }

    <T extends ServerToClientEvent> T assertOneWsEventIs(final Class<? extends T> klass) {
        Assertions.assertFalse(this.receivedEvents.isEmpty());
        var event = this.receivedEvents.removeFirst();
        Assertions.assertTrue(this.receivedEvents.isEmpty());
        Assertions.assertInstanceOf(klass, event);
        return klass.cast(event);
    }

    @Test
    void createListAddsEventToWs() {
        Board b = this.createBoard();
        CardList list;

        try (var s = this.initWebsocket(b.getId(), 1)) {
            list = this.createCardList(b.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ListCreatedEvent.class);
        Assertions.assertEquals(list.getId(), e.list().getId());
    }

    @Test
    void createCardAddsEventToWs() {
        Board b = this.createBoard();
        CardList list = this.createCardList(b.getId());
        Card card;

        try (var s = this.initWebsocket(b.getId(), 1)) {
            card = this.createCard(b.getId(), list.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(CardCreatedEvent.class);
        Assertions.assertEquals(list.getId(), e.cardListId());
        Assertions.assertEquals(card.getId(), e.card().getId());
    }

    @Test
    void removeListAddsEventToWs() {
        Board b = this.createBoard();
        CardList list = this.createCardList(b.getId());

        try (var s = this.initWebsocket(b.getId(), 1)) {
            this.removeCardList(b.getId(), list.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ListRemovedEvent.class);

        Assertions.assertEquals(list.getId(), e.cardList().getId());
    }

    /**
     * Tests if moving a CardList to a new position on the board triggers a WebSocket event.
     */
    @Test
    void moveListAddsEventToWS() {
        // Create a new board and get the ID of the board.
        final Board board = this.createBoard();
        final long boardId = board.getId();

        // Generate a list of 3 `CardList` objects using `createCardList()` method as the generator function.
        final int nrOfLists = 3;
        final List<CardList> cardLists = Stream.generate(() -> this.createCardList(boardId)).limit(nrOfLists).toList();

        // Try to initialize a WebSocket, move the first list to the third position on the board.
        try (var ignored = this.initWebsocket(board.getId(), 1)) {
            this.moveCardList(boardId, cardLists.get(0).getId(), cardLists.get(2).getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        // Cast the `event` object accordingly to a `ListsReorderedEvent` object.
        ListsReorderedEvent listsReorderedEvent = this.assertOneWsEventIs(ListsReorderedEvent.class);

        Assertions.assertEquals(cardLists.get(0).getId(), listsReorderedEvent.cardList(),
                "Card list in the event should be the first list.");
        Assertions.assertEquals(cardLists.get(2).getId(), listsReorderedEvent.placedAfter(),
                "New position of the card list in the event should be the third list.");
    }

    @Test
    void setCardTitleAddsEventToWs() {
        Board b = this.createBoard();
        CardList list = this.createCardList(b.getId());
        Card card = this.createCard(b.getId(), list.getId());

        final String newTitle = "New title";

        try (var s = this.initWebsocket(b.getId(), 1)) {
            this.setCardTitle(b.getId(), list.getId(), card.getId(), newTitle);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(CardTitleSetEvent.class);

        Assertions.assertEquals(card.getId(), e.cardId());
        Assertions.assertEquals(newTitle, e.newTitle());
    }

    @Test
    void setCardTextAddsEventToWs() {
        Board b = this.createBoard();
        CardList list = this.createCardList(b.getId());
        Card card = this.createCard(b.getId(), list.getId());

        final String newText = "New text";

        try (var s = this.initWebsocket(b.getId(), 1)) {
            this.setCardText(b.getId(), list.getId(), card.getId(), newText);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(CardTextSetEvent.class);

        Assertions.assertEquals(card.getId(), e.cardId());
        Assertions.assertEquals(newText, e.newText());
    }

    @Test
    void setCardDueDateAddsEventToWs() {
        Board b = this.createBoard();
        CardList list = this.createCardList(b.getId());
        Card card = this.createCard(b.getId(), list.getId());

        final ZonedDateTime newDueDate = ZonedDateTime.now();

        try (var s = this.initWebsocket(b.getId(), 1)) {
            this.setCardDueDate(b.getId(), list.getId(), card.getId(), newDueDate);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(CardDueDateSetEvent.class);

        Assertions.assertEquals(card.getId(), e.cardId());
        this.assertZDTEquals(newDueDate, e.dueDate());
    }

    @Test
    void setCardListTitleAddsEventToWs() {
        Board b = this.createBoard();
        CardList list = this.createCardList(b.getId());

        final String newTitle = "New title";

        try (var s = this.initWebsocket(b.getId(), 1)) {
            this.setCardListTitle(b.getId(), list.getId(), newTitle);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(CardListTitleSetEvent.class);

        Assertions.assertEquals(list.getId(), e.cardListId());
        Assertions.assertEquals(newTitle, e.newTitle());
    }

    @Test
    void setBoardTitleTest() {
        var board = this.createBoard();
        //var cardList = createCardList(board.getId());

        var newTitle = "New title";

        this.setBoardTitle(board.getId(), newTitle);

        var board2 = this.getBoard(board.getId());

        Assertions.assertNotEquals(board, board2);
        Assertions.assertEquals(newTitle, board2.getTitle());
    }

    @Test
    void setBoardTitleAddsEventToWs() {
        Board b = this.createBoard();
        //CardList list = createCardList(b.getId());

        final String newTitle = "New title";

        try (var s = this.initWebsocket(b.getId(), 1)) {
            this.setBoardTitle(b.getId(), newTitle);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(BoardTitleSetEvent.class);

        Assertions.assertEquals(b.getId(), e.boardId());
        Assertions.assertEquals(newTitle, e.newTitle());
    }

    @Test
    void deleteCardListAddsEventToWs() {
        Board b = this.createBoard();
        CardList list = this.createCardList(b.getId());
        try (var s = this.initWebsocket(b.getId(), 1)) {
            this.removeCardList(b.getId(), list.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ListRemovedEvent.class);

        Assertions.assertEquals(list.getId(), e.cardList().getId());
    }

    @Test
    void tagCreateAddsEventToWs() {
        Board b = this.createBoard();
        Tag t;
        try (var s = this.initWebsocket(b.getId(), 1)) {
            t = this.createTag(b.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(TagCreatedEvent.class);

        Assertions.assertEquals(t.getId(), e.tag().getId());
    }

    @Test
    void tagDeletedAddsEventToWs() {
        Board b = this.createBoard();
        Tag t = this.createTag(b.getId());

        try (var s = this.initWebsocket(b.getId(), 1)) {
            this.deleteTag(b.getId(), t.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(TagDeletedEvent.class);

        Assertions.assertEquals(t.getId(), e.tag().getId());
    }

    @Test
    void tagNameSetAddsEventToWs() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());
        String newName = "New Name";
        this.setTagName(board.getId(), tag.getId(), newName);

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setTagName(board.getId(), tag.getId(), newName);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(TagNameSetEvent.class);

        Assertions.assertEquals(tag.getId(), e.tag().getId());
    }

    @Test
    void xlistMoveAddsEventToWs() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var cardList2 = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        var card2 = this.createCard(board.getId(), cardList2.getId());
        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.xlistMoveCard(board.getId(), cardList.getId(), card.getId(), cardList2.getId(), card2.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(XListCardMoveEvent.class);

        Assertions.assertEquals(cardList.getId(), e.srcCardList());
        Assertions.assertEquals(card.getId(), e.card());
        Assertions.assertEquals(cardList2.getId(), e.destCardList());
        Assertions.assertEquals(card2.getId(), e.hook());
    }

    @Test
    void tagModificationsAddEventsToWs() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        var tag = this.createTag(board.getId());

        try (var s = this.initWebsocket(board.getId(), 2)) {
            this.addTagToCard(board.getId(), cardList.getId(), card.getId(), tag.getId());
            this.removeTagFromCard(board.getId(), cardList.getId(), card.getId(), tag.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(2, this.receivedEvents.size());
        var event = this.receivedEvents.removeFirst();
        var event2 = this.receivedEvents.removeFirst();
        Assertions.assertTrue(this.receivedEvents.isEmpty());
        Assertions.assertInstanceOf(CardTagAddedEvent.class, event);
        Assertions.assertInstanceOf(CardTagRemovedEvent.class, event2);
        var e = (CardTagAddedEvent) event;
        var e2 = (CardTagRemovedEvent) event2;

        Assertions.assertEquals(card.getId(), e.cardId());
        Assertions.assertEquals(tag.getId(), e.tagId());

        Assertions.assertEquals(card.getId(), e2.cardId());
        Assertions.assertEquals(tag.getId(), e2.tagId());
    }

    @Test
    void setBoardFontColorAddsEventToWs() {
        var board = this.createBoard();

        String newFontColor = "aaaa";

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setBoardFontColor(board.getId(), newFontColor);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(BoardFontColorSetEvent.class);

        Assertions.assertEquals(newFontColor, e.fontColor());
    }

    @Test
    void setBoardFontColorToNullAddsEventToWs() {
        var board = this.createBoard();

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setBoardFontColor(board.getId(), null);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(BoardFontColorSetEvent.class);

        Assertions.assertNull(e.fontColor());
    }

    @Test
    void setCardListFontColorAddsEventToWs() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());

        String newFontColor = "aaaa";

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setCardListFontColor(board.getId(), cardList.getId(), newFontColor);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ListFontColorSetEvent.class);

        Assertions.assertEquals(cardList.getId(), e.listId());
        Assertions.assertEquals(newFontColor, e.newFont());
    }

    @Test
    void setCardListFontColorToNullAddsEventToWs() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setCardListFontColor(board.getId(), cardList.getId(), null);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ListFontColorSetEvent.class);

        Assertions.assertEquals(cardList.getId(), e.listId());
        Assertions.assertNull(e.newFont());
    }

    @Test
    void createPresetAddsEventToWs() {
        var board = this.createBoard();
        ColorPreset created;

        try (var s = this.initWebsocket(board.getId(), 1)) {
            created = this.createPreset(board.getId(), new ColorPreset());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ColorPresetCreatedEvent.class);

        Assertions.assertEquals(created.getId(), e.preset().getId());
    }

    @Test
    void deletePresetAddsEventToWs() {
        var board = this.createBoard();
        ColorPreset preset = this.createPreset(board.getId(), new ColorPreset());

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.deletePreset(board.getId(), preset.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ColorPresetRemovedEvent.class);

        Assertions.assertEquals(preset.getId(), e.preset().getId());
    }

    @Test
    void setColorPresetNameAddsEventToWs() {
        var board = this.createBoard();
        ColorPreset preset = this.createPreset(board.getId(), new ColorPreset());

        String newName = "aaa";

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setPresetName(board.getId(), preset.getId(), newName);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ColorPresetNameSetEvent.class);

        Assertions.assertEquals(preset.getId(), e.presetKey());
        Assertions.assertEquals(newName, e.name());
    }

    @Test
    void setColorPresetFontColorAddsEventToWs() {
        var board = this.createBoard();
        ColorPreset preset = this.createPreset(board.getId(), new ColorPreset());

        String newFontColor = "aaa";

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setPresetFontColor(board.getId(), preset.getId(), newFontColor);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ColorPresetFontColorSetEvent.class);

        Assertions.assertEquals(preset.getId(), e.presetKey());
        Assertions.assertEquals(newFontColor, e.fontColor());
    }

    @Test
    void setColorPresetFontColorToNullAddsEventToWs() {
        var board = this.createBoard();
        ColorPreset preset = this.createPreset(board.getId(), new ColorPreset());

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setPresetFontColor(board.getId(), preset.getId(), null);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ColorPresetFontColorSetEvent.class);

        Assertions.assertEquals(preset.getId(), e.presetKey());
        Assertions.assertNull(e.fontColor());
    }

    @Test
    void setColorPresetBackgroundColorAddsEventToWs() {
        var board = this.createBoard();
        ColorPreset preset = this.createPreset(board.getId(), new ColorPreset());

        String newBackgroundColor = "aaa";

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setPresetBackground(board.getId(), preset.getId(), newBackgroundColor);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ColorPresetBackgroundColorSetEvent.class);

        Assertions.assertEquals(preset.getId(), e.presetKey());
        Assertions.assertEquals(newBackgroundColor, e.background());
    }

    @Test
    void setColorPresetBackgroundColorToNullAddsEventToWs() {
        var board = this.createBoard();
        ColorPreset preset = this.createPreset(board.getId(), new ColorPreset());

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setPresetBackground(board.getId(), preset.getId(), null);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ColorPresetBackgroundColorSetEvent.class);

        Assertions.assertEquals(preset.getId(), e.presetKey());
        Assertions.assertNull(e.background());
    }

    @Test
    void setDefaultPresetAddsEventToWs() {
        var board = this.createBoard();
        var preset = this.createPreset(board.getId(), new ColorPreset());

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setDefaultPreset(board.getId(), preset.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(DefaultCardColorPresetSetEvent.class);

        Assertions.assertEquals(preset.getId(), e.presetKey());
    }

    @Test
    void setCardPresetAddsEventToWs() {
        var board = this.createBoard();
        var preset = this.createPreset(board.getId(), new ColorPreset());
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setCardPreset(board.getId(), cardList.getId(), card.getId(), preset.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(CardPresetSetEvent.class);

        Assertions.assertEquals(card.getId(), e.cardId());
        Assertions.assertEquals(preset.getId(), e.presetKey());
    }

    @Test
    void deletePresetRemovesFromCardsAddsEventToWs() {
        var board = this.createBoard();
        var preset = this.createPreset(board.getId(), new ColorPreset());
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        this.setCardPreset(board.getId(), cardList.getId(), card.getId(), preset.getId());

        try (var s = this.initWebsocket(board.getId(), 2)) {
            this.deletePreset(board.getId(), preset.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(2, this.receivedEvents.size());
        var event1 = this.receivedEvents.removeFirst();
        var event2 = this.receivedEvents.removeFirst();
        Assertions.assertTrue(this.receivedEvents.isEmpty());
        Assertions.assertInstanceOf(ColorPresetRemovedEvent.class, event1);
        Assertions.assertInstanceOf(CardPresetSetEvent.class, event2);

        var e1 = (ColorPresetRemovedEvent) event1;
        var e2 = (CardPresetSetEvent) event2;

        Assertions.assertEquals(preset.getId(), e1.preset().getId());

        Assertions.assertEquals(0, e2.presetKey());
        Assertions.assertEquals(card.getId(), e2.cardId());
    }

    @Test
    void setBoardBackgroundColorAddsEventToWs() {
        var board = this.createBoard();

        String newBackgroundColor = "aaaa";

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setBoardBackgroundColor(board.getId(), newBackgroundColor);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(BoardBackgroundColorSetEvent.class);

        Assertions.assertEquals(newBackgroundColor, e.background());
    }

    @Test
    void setBoardBackgroundColorToNullAddsEventToWs() {
        var board = this.createBoard();

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setBoardBackgroundColor(board.getId(), null);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(BoardBackgroundColorSetEvent.class);

        Assertions.assertNull(e.background());
    }

    @Test
    void setCardListBackgroundColorAddsEventToWs() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());

        String newBackgroundColor = "aaaa";

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setCardListBackgroundColor(board.getId(), cardList.getId(), newBackgroundColor);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ListBackgroundColorSetEvent.class);

        Assertions.assertEquals(cardList.getId(), e.listId());
        Assertions.assertEquals(newBackgroundColor, e.newBackground());
    }

    @Test
    void setCardListBackgroundColorToNullAddsEventToWs() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setCardListBackgroundColor(board.getId(), cardList.getId(), null);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(ListBackgroundColorSetEvent.class);

        Assertions.assertEquals(cardList.getId(), e.listId());
        Assertions.assertNull(e.newBackground());
    }

    @Test
    void setTagBackgroundColorAddsEventToWs() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());

        String newBackgroundColor = "aaaa";

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setTagBackgroundColor(board.getId(), tag.getId(), newBackgroundColor);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(TagBackgroundColorSetEvent.class);

        Assertions.assertEquals(newBackgroundColor, e.newBackgroundColor());
    }

    @Test
    void setTagBackgroundColorToNullAddsEventToWs() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setTagBackgroundColor(board.getId(), tag.getId(), null);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(TagBackgroundColorSetEvent.class);

        Assertions.assertNull(e.newBackgroundColor());
    }


    @Test
    void setTagFontColorAddsEventToWs() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());

        String newFontColor = "aaaa";

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setTagFontColor(board.getId(), tag.getId(), newFontColor);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(TagFontColorSetEvent.class);

        Assertions.assertEquals(newFontColor, e.newFontColor());
    }

    @Test
    void setTagFontColorToNullAddsEventToWs() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());

        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setTagFontColor(board.getId(), tag.getId(), null);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(TagFontColorSetEvent.class);

        Assertions.assertNull(e.newFontColor());
    }

    @Test
    void createSubtaskAddsEventToWs() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        CardSubtask subtask;
        try (var s = this.initWebsocket(board.getId(), 1)) {
            subtask = this.createSubtask(board.getId(), cardList.getId(), card.getId(), new CardSubtask());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(CardSubtaskCreatedEvent.class);

        Assertions.assertEquals(card.getId(), e.cardId());
        Assertions.assertEquals(subtask.getId(), e.subtask().getId());
    }

    @Test
    void deleteSubtaskAddsEventToWs() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        var subtask = this.createSubtask(board.getId(), cardList.getId(), card.getId(), new CardSubtask());
        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.deleteSubtask(board.getId(), cardList.getId(), card.getId(), subtask.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(CardSubtaskRemovedEvent.class);

        Assertions.assertEquals(card.getId(), e.cardId());
        Assertions.assertEquals(subtask.getId(), e.subtaskId());
    }

    @Test
    void setSubtaskNameAddsEventToWs() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        var subtask = this.createSubtask(board.getId(), cardList.getId(), card.getId(), new CardSubtask());
        String newName = "aaaa";
        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setSubtaskName(board.getId(), cardList.getId(), card.getId(), subtask.getId(), newName);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(CardSubtaskNameSetEvent.class);
        Assertions.assertEquals(subtask.getId(), e.subtaskId());
        Assertions.assertEquals(newName, e.newName());
    }

    @Test
    void setSubtaskCompletenessAddsEventToWs() {
        var board = this.createBoard();
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        var subtask = this.createSubtask(board.getId(), cardList.getId(), card.getId(), new CardSubtask());
        boolean newCompleteness = true;
        try (var s = this.initWebsocket(board.getId(), 1)) {
            this.setSubtaskCompleteness(board.getId(), cardList.getId(), card.getId(), subtask.getId(),
                    newCompleteness);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        var e = this.assertOneWsEventIs(CardSubtaskCompletenessSetEvent.class);
        Assertions.assertEquals(subtask.getId(), e.subtaskId());
        Assertions.assertEquals(newCompleteness, e.newCompleteness());
    }

    @Test
    void deleteTagRemovesFromCardAddsEventsToWs() {
        var board = this.createBoard();
        var tag = this.createTag(board.getId());
        var cardList = this.createCardList(board.getId());
        var card = this.createCard(board.getId(), cardList.getId());
        this.addTagToCard(board.getId(), cardList.getId(), card.getId(), tag.getId());

        try (var s = this.initWebsocket(board.getId(), 2)) {
            this.deleteTag(board.getId(), tag.getId());
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        Assertions.assertEquals(2, this.receivedEvents.size());
        var event1 = this.receivedEvents.removeFirst();
        var event2 = this.receivedEvents.removeFirst();
        Assertions.assertTrue(this.receivedEvents.isEmpty());

        Assertions.assertInstanceOf(TagDeletedEvent.class, event1);
        Assertions.assertInstanceOf(CardTagRemovedEvent.class, event2);

        var e1 = (TagDeletedEvent) event1;
        var e2 = (CardTagRemovedEvent) event2;

        Assertions.assertEquals(tag.getId(), e1.tag().getId());

        Assertions.assertEquals(card.getId(), e2.cardId());
        Assertions.assertEquals(tag.getId(), e2.tagId());
    }

    private static class SetParameterizedTypeReference extends ParameterizedTypeReference<Set<Tag>> {
        // Purposefully left empty because this is used as a type token
    }
}
