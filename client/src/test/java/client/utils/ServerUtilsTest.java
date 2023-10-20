package client.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import commons.Board;
import commons.Card;
import commons.CardList;
import commons.CardSubtask;
import commons.Tag;
import jakarta.ws.rs.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@WireMockTest
class ServerUtilsTest {
    private static final int I_AM_A_TEAPOT = 418;
    @Mock
    private WebsocketClientEndpoint mockWsClient;

    private final Injector injector = Guice.createInjector(new MyTestModule());
    private ServerUtilsInterface serverUtils;

    private WireMockServer wireMockServer;

    private static final long BOARD_ID = 1L;


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.serverUtils = this.injector.getInstance(ServerUtils.class);

        this.wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        this.wireMockServer.start();
        WireMock.configureFor(this.wireMockServer.port());
    }

    @AfterEach
    void teardown() {
        this.wireMockServer.stop();
    }

    private String setupServerAndConnection() throws Exception {
        // Get the URL
        String url = this.wireMockServer.baseUrl();

        // Set up WireMock stubs
        this.wireMockServer.stubFor(WireMock.get("/").willReturn(WireMock.status(I_AM_A_TEAPOT)));

        this.serverUtils.validateAndSetServer(url);
        this.serverUtils.subscribeToBoard(BOARD_ID);

        return url;
    }

    @Test
    void testValidateAndSetServer() throws Exception {
        String url = this.setupServerAndConnection();

        // Verify the result
        this.wireMockServer.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/")));

        URI httpURI = new URI(url);
        URI wsURI = new URI("ws", httpURI.getUserInfo(), httpURI.getHost(), httpURI.getPort(), httpURI.getPath(),
                httpURI.getQuery(), httpURI.getFragment()).resolve("/board");

        Assertions.assertEquals(httpURI, this.serverUtils.getHttpServerURI(),
                "The http server URI should be set correctly.");
        Assertions.assertEquals(wsURI, this.serverUtils.getWsServerURI(),
                "The websocket server URI should be set correctly.");
    }

    @Test
    void testValidateAndSetServerThrowsInvalidServerException() throws Exception {
        String url = this.setupServerAndConnection();

        // Set up WireMock stubs
        String expectedResponse = "not a teapot";
        this.wireMockServer.stubFor(WireMock.get("/").willReturn(WireMock.ok(expectedResponse)));

        // Verify the result
        Assertions.assertThrows(InvalidServerException.class, () -> {
            this.serverUtils.validateAndSetServer(url);
        });
        this.wireMockServer.verify(2, WireMock.getRequestedFor(WireMock.urlEqualTo("/")));
    }

    @Test
    void testValidateAndSetServerNoBoards() throws Exception {
        // Get the URL
        String url = this.wireMockServer.baseUrl();

        // Set up WireMock stubs
        this.wireMockServer.stubFor(WireMock.get("/").willReturn(WireMock.status(I_AM_A_TEAPOT)));

        String expectedResBody = "[]";
        this.wireMockServer.stubFor(WireMock.get("/boards/all").willReturn(
                WireMock.aResponse().withHeader("Content-Type", "application/json").withBody(expectedResBody)));

        String expectedResBody2 = "{\"id\": %d, \"cardLists\": []}".formatted(BOARD_ID);
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("/boards")).willReturn(
                WireMock.aResponse().withHeader("Content-Type", "application/json").withBody(expectedResBody2)));

        // Call the method being tested
        this.serverUtils.validateAndSetServer(url);

        // Verify the result
        this.wireMockServer.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/")));

        URI httpURI = new URI(url);
        URI wsURI = new URI("ws", httpURI.getUserInfo(), httpURI.getHost(), httpURI.getPort(), httpURI.getPath(),
                httpURI.getQuery(), httpURI.getFragment()).resolve("/board");

        Assertions.assertEquals(httpURI, this.serverUtils.getHttpServerURI(),
                "The http server URI should be set correctly.");
        Assertions.assertEquals(wsURI, this.serverUtils.getWsServerURI(),
                "The websocket server URI should be set correctly.");
    }

    @Test
    void testGetBoard() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        String expectedResBody = "{\"id\": %d, \"cardLists\": []}".formatted(BOARD_ID);

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/boards/%d".formatted(BOARD_ID))).willReturn(
                WireMock.aResponse().withHeader("Content-Type", "application/json").withBody(expectedResBody)));

        // Call the method being tested
        Board actualBoard = this.serverUtils.getBoard(BOARD_ID);

        // Verify the result
        Assertions.assertEquals(BOARD_ID, actualBoard.getId(),
                "The board from the server should match the board returned from the method.");
        this.wireMockServer.verify(1,
                WireMock.getRequestedFor(WireMock.urlEqualTo("/boards/%d".formatted(BOARD_ID))));
    }

    @Test
    void getBoardThrowsTest() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/boards/%d".formatted(BOARD_ID))).willReturn(
                WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            Board board = this.serverUtils.getBoard(BOARD_ID);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.getRequestedFor(WireMock.urlEqualTo("/boards/%d".formatted(BOARD_ID))));
    }

    @Test
    void testGetBoardNoParams() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        String expectedResBody = "{\"id\": %d, \"cardLists\": []}".formatted(BOARD_ID);

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/boards/%d".formatted(BOARD_ID))).willReturn(
                WireMock.aResponse().withHeader("Content-Type", "application/json").withBody(expectedResBody)));

        // Call the method being tested
        Board actualBoard = this.serverUtils.getBoard();

        // Verify the result
        Assertions.assertEquals(BOARD_ID, actualBoard.getId(),
                "The board from the server should match the board returned from the method.");
        this.wireMockServer.verify(1,
                WireMock.getRequestedFor(WireMock.urlEqualTo("/boards/%d".formatted(BOARD_ID))));
    }

    @Test
    void testGetBoards() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        String expectedResBody = "[]";

        this.wireMockServer.stubFor(WireMock.get(WireMock.urlMatching("/boards/all"))
                .willReturn(
                        WireMock.aResponse().withHeader("Content-Type", "application/json").withBody(expectedResBody)));

        // Call the method being tested
        List<Board> returnedBoardList = this.serverUtils.getBoards();

        // Verify the result
        Assertions.assertEquals(0, returnedBoardList.size(),
                "The list of boards returned from by the server should be empty but not null.");
        this.wireMockServer.verify(1,
                WireMock.getRequestedFor(WireMock.urlEqualTo("/boards/all")));
    }

    @Test
    void testAddBoard() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        String expectedResBody = "{\"id\": %d, \"cardLists\": []}".formatted(BOARD_ID);

        this.wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/boards")).willReturn(
                WireMock.aResponse().withHeader("Content-Type", "application/json").withBody(expectedResBody)));

        // Call the method being tested
        Board returnedBoard = this.serverUtils.addBoard();

        // Verify the result
        Assertions.assertEquals(BOARD_ID, returnedBoard.getId(),
                "The board returned from by the server should match the board returned from the method.");
        this.wireMockServer.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/boards")));
    }

    @Test
    void testAddList() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        String expectedResBody = "{\"id\":%d,\"cards\":[]}".formatted(listId);

        this.wireMockServer.stubFor(WireMock.post(WireMock.urlMatching("/boards/%d/lists".formatted(BOARD_ID)))
                .willReturn(
                        WireMock.aResponse().withHeader("Content-Type", "application/json").withBody(expectedResBody)));

        // Call the method being tested
        CardList returnedList = this.serverUtils.addList();

        // Verify the result
        Assertions.assertEquals(listId, returnedList.getId(),
                "The list returned from by the server should match the list returned from the method.");
        this.wireMockServer.verify(1,
                WireMock.postRequestedFor(WireMock.urlEqualTo("/boards/%d/lists".formatted(BOARD_ID))));
    }

    @Test
    void testGetList() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        String expectedResBody = "{\"id\":%d,\"cards\":[]}".formatted(listId);

        this.wireMockServer.stubFor(
                WireMock.get(WireMock.urlMatching("/boards/%d/lists/%d".formatted(BOARD_ID, listId)))
                .willReturn(
                        WireMock.aResponse().withHeader("Content-Type", "application/json")
                                .withBody(expectedResBody)));

        // Call the method being tested
        CardList returnedList = this.serverUtils.getList(listId);

        // Verify the result
        Assertions.assertEquals(listId, returnedList.getId(),
                "The list from the server should match the list returned from the method.");
        this.wireMockServer.verify(1,
                WireMock.getRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d".formatted(BOARD_ID, listId))));
    }

    @Test
    void testSetListTitle() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        String newTitle = "new title";
        String expectedResBody =
                "{\"title\":\"%s\",\"id\":%d,\"cards\":[]}".formatted(newTitle, listId);
        // Stub the server response
        this.wireMockServer.stubFor(WireMock.get(WireMock
                        .urlMatching("/boards/%d/lists/%d".formatted(BOARD_ID, listId)))
                .willReturn(
                        WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(expectedResBody)));
        this.wireMockServer.stubFor(WireMock.put(WireMock.urlMatching("/boards/%d/lists/%d/list_title"
                        .formatted(BOARD_ID, listId)))
                .willReturn(
                        WireMock.aResponse().withHeader("Content-Type", "application/json")
                                .withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.setListTitle(listId, newTitle);
        CardList returnedList = this.serverUtils.getList(listId);

        // Verify the result
        Assertions.assertEquals(newTitle, returnedList.getTitle(),
                "The list title from the server should be set to the new title.");
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/list_title"
                        .formatted(BOARD_ID, listId))));
        this.wireMockServer.verify(1,
                WireMock.getRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d".formatted(BOARD_ID, listId))));
    }

    @Test
    void testSetListTitleThrows() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        String newTitle = "new title";

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock.urlMatching("/boards/%d/lists/%d/list_title"
                        .formatted(BOARD_ID, listId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.setListTitle(listId, newTitle);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/list_title"
                        .formatted(BOARD_ID, listId))));
    }

    @Test
    void testAddCard() throws Exception {
        // todo: add a dueDate to the card.
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long cardId = 1L;
        final long listId = 2L;
        final Card cardToAdd = new Card();
        final String cardTitle = "New Card Title";
        final String cardDescription = "New Card Description";
        final String cardCategory = "New Card Category";
        cardToAdd.patch(cardPatcher -> {
            cardPatcher.setTitle(cardTitle);
            cardPatcher.setText(cardDescription);
            cardPatcher.setCategory(cardCategory);
        });

        // Stub the server response
        this.wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/boards/%d/lists/%d/cards".formatted(BOARD_ID, listId)))
                        .withRequestBody(WireMock.matchingJsonPath("$.id", WireMock.equalTo("0")))
                        .withRequestBody(WireMock.matchingJsonPath("$.title", WireMock.equalTo(cardTitle)))
                        .withRequestBody(WireMock.matchingJsonPath("$.text", WireMock.equalTo(cardDescription)))
                        .withRequestBody(WireMock.matchingJsonPath("$.category", WireMock.equalTo(cardCategory)))
                        .willReturn(WireMock.okJson(
                                "{\"id\": %d, \"title\": \"%s\", \"text\": \"%s\", \"category\": \"%s\"}".formatted(
                                        cardId, cardTitle, cardDescription, cardCategory))));

        // Call the method being tested
        Card addedCard = this.serverUtils.addCard(listId, cardToAdd);

        // Verify the server request
        this.wireMockServer.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo("/boards/1/lists/2/cards"))
                .withRequestBody(WireMock.matchingJsonPath("$.id", WireMock.equalTo("0")))
                .withRequestBody(WireMock.matchingJsonPath("$.title", WireMock.equalTo(cardTitle)))
                .withRequestBody(WireMock.matchingJsonPath("$.text", WireMock.equalTo(cardDescription)))
                .withRequestBody(WireMock.matchingJsonPath("$.category", WireMock.equalTo(cardCategory))));

        // Verify the returned card
        Assertions.assertEquals(cardId, addedCard.getId(), "The returned card ID should be correct.");
        Assertions.assertEquals(cardTitle, addedCard.getTitle(), "The returned card title should be correct.");
        Assertions.assertEquals(cardDescription, addedCard.getText(),
                "The returned card description should be correct.");
        Assertions.assertEquals(cardCategory, addedCard.getCategory(), "The returned card category should be correct.");
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
           Card returned = this.serverUtils.addCard(listId, addedCard);
        });
    }

    @Test
    void testGetCard() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 2L;
        String expectedResBody = "{\"id\": %d, \"title\": \"\", \"text\": \"\", \"category\": \"\"}".formatted(cardId);

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.get(WireMock
                        .urlMatching("/boards/%d/lists/%d/cards/%d"
                                .formatted(BOARD_ID, listId, cardId)))
                .willReturn(
                        WireMock.aResponse().withHeader("Content-Type", "application/json").withBody(expectedResBody)));

        // Call the method being tested
        Card returnedCard = this.serverUtils.getCard(listId, cardId);

        // Verify the result
        Assertions.assertEquals(cardId, returnedCard.getId(),
                "The card from the server should match the card returned from the method.");
        this.wireMockServer.verify(1,
                WireMock.getRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/lists/%d/cards/%d"
                                .formatted(BOARD_ID, listId, cardId))));
    }

    @Test
    void testSetCardTitle() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        String newTitle = "new title";

        // Stub the server response
        this.wireMockServer.stubFor(WireMock
                .put(WireMock.urlMatching("/boards/%d/lists/%d/cards/%d/card_title"
                        .formatted(BOARD_ID, listId, cardId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.setCardTitle(listId, cardId, newTitle);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/card_title"
                        .formatted(BOARD_ID, listId, cardId))));
    }

    @Test
    void setCardTitleThrowsTest() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        String newTitle = "new title";

        // Stub the server response
        this.wireMockServer.stubFor(WireMock
                .put(WireMock.urlMatching("/boards/%d/lists/%d/cards/%d/card_title"
                        .formatted(BOARD_ID, listId, cardId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.setCardTitle(listId, cardId, newTitle);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/card_title"
                        .formatted(BOARD_ID, listId, cardId))));
    }

    @Test
    void testSetCardText() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        String newText = "new text";

        // Stub the server response
        this.wireMockServer.stubFor(WireMock
                .put(WireMock.urlMatching("/boards/%d/lists/%d/cards/%d/card_text"
                        .formatted(BOARD_ID, listId, cardId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.setCardText(listId, cardId, newText);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/card_text"
                        .formatted(BOARD_ID, listId, cardId))));
    }

    @Test
    void setCardTextThrowsTest() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        String newText = "new text";

        // Stub the server response
        this.wireMockServer.stubFor(WireMock
                .put(WireMock.urlMatching("/boards/%d/lists/%d/cards/%d/card_text"
                        .formatted(BOARD_ID, listId, cardId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.setCardText(listId, cardId, newText);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/card_text"
                        .formatted(BOARD_ID, listId, cardId))));
    }

    @Test
    void testSetCardDueDate() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        String newDueDate = "2023-03-29T10:15:30+01:00[Europe/Amsterdam]";

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/lists/%d/cards/%d/due_date"
                                .formatted(BOARD_ID, listId, cardId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.setCardDueDate(listId, cardId, newDueDate);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/due_date"
                        .formatted(BOARD_ID, listId, cardId))));
    }

    @Test
    void setCardDueDateThrowsTest() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        String newDueDate = "2023-03-29T10:15:30+01:00[Europe/Amsterdam]";

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/lists/%d/cards/%d/due_date"
                                .formatted(BOARD_ID, listId, cardId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.setCardDueDate(listId, cardId, newDueDate);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/due_date"
                        .formatted(BOARD_ID, listId, cardId))));
    }

    @Test
    void testSameListCardMove() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        final long otherCardId = 2L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock
                .put(WireMock.urlMatching("/boards/%d/lists/%d/cards/%d/moveAfter/%d"
                        .formatted(BOARD_ID, listId, cardId, otherCardId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.sameListCardMove(listId, cardId, otherCardId);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/moveAfter/%d"
                        .formatted(BOARD_ID, listId, cardId, otherCardId))));
    }

    @Test
    void sameListCardMoveThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        final long otherCardId = 2L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock
                .put(WireMock.urlMatching("/boards/%d/lists/%d/cards/%d/moveAfter/%d"
                        .formatted(BOARD_ID, listId, cardId, otherCardId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.sameListCardMove(listId, cardId, otherCardId);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/moveAfter/%d"
                        .formatted(BOARD_ID, listId, cardId, otherCardId))));
    }

    @Test
    void testxListCardMove() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        final long otherListId = 2L;
        final long otherCardId = 2L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock
                .put(WireMock.urlMatching("/boards/%d/lists/%d/cards/%d/xListMoveAfter/%d/%d"
                        .formatted(BOARD_ID, listId, cardId, otherListId, otherCardId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.xListCardMove(listId, cardId, otherListId, otherCardId);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/xListMoveAfter/%d/%d"
                        .formatted(BOARD_ID, listId, cardId, otherListId, otherCardId))));
    }

    @Test
    void xListCardMoveThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        final long otherListId = 2L;
        final long otherCardId = 2L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock
                .put(WireMock.urlMatching("/boards/%d/lists/%d/cards/%d/xListMoveAfter/%d/%d"
                        .formatted(BOARD_ID, listId, cardId, otherListId, otherCardId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.xListCardMove(listId, cardId, otherListId, otherCardId);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/xListMoveAfter/%d/%d"
                        .formatted(BOARD_ID, listId, cardId, otherListId, otherCardId))));
    }

    @Test
    void testDeleteList() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching("/boards/%d/lists/%d"
                        .formatted(BOARD_ID, listId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.deleteList(listId);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.deleteRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d"
                        .formatted(BOARD_ID, listId))));
    }

    @Test
    void testDeleteListThrows() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching("/boards/%d/lists/%d"
                        .formatted(BOARD_ID, listId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.deleteList(listId);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.deleteRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d"
                        .formatted(BOARD_ID, listId))));
    }

    @Test
    void testDeleteCard() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 2L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching("/boards/%d/lists/%d/cards/%d"
                        .formatted(BOARD_ID, listId, cardId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.deleteCard(listId, cardId);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.deleteRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d"
                        .formatted(BOARD_ID, listId, cardId))));
    }

    @Test
    void testDeleteCardThrows() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 2L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching("/boards/%d/lists/%d/cards/%d"
                        .formatted(BOARD_ID, listId, cardId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.deleteCard(listId, cardId);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.deleteRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d"
                        .formatted(BOARD_ID, listId, cardId))));
    }

    @Test
    void testNewTag() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final Tag tagToAdd = new Tag();
        final String tagTitle = "New Title";
        final String fontColor = "";
        final String backgroundColor = "";
        final long tagId = 20;

        // Stub the server response
        this.wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/boards/%d/tags".formatted(BOARD_ID)))
                        .willReturn(WireMock.okJson(
                                ("{\"id\": %d, \"name\": \"%s\", \"fontColor\": \"%s\", \"backgroundColor\": \"%s\"," +
                                        " \"boardId\": \"%d\"}").formatted(
                                        tagId, tagTitle, fontColor, backgroundColor, BOARD_ID))));

        // Call the method being tested
        Tag addedTag = this.serverUtils.newTag(tagToAdd);

        // Verify the server request
        this.wireMockServer.verify(1, WireMock.postRequestedFor(WireMock
                .urlEqualTo("/boards/%d/tags".formatted(BOARD_ID))));

        // Verify the returned card
        Assertions.assertEquals(tagId, addedTag.getId(), "The returned card ID should be correct.");
        Assertions.assertEquals(tagTitle, addedTag.getName(), "The returned tag name should be correct.");
        Assertions.assertEquals(fontColor, addedTag.getFontColor(),
                "The returned tag font color should be correct.");
        Assertions.assertEquals(backgroundColor, addedTag.getBackgroundColor(),
                "The returned tag background color should be correct.");
    }

    @Test
    void testGetTag() throws Exception {
        // Set up WireMock stubs
        this.setupServerAndConnection();

        final long tagId = 2L;
        String expectedResBody = ("{\"id\": %d, \"name\": \"\", \"fontColor\": \"\"," +
                " \"backgroundColor\": \"\", \"boardId\": %d}").formatted(tagId, BOARD_ID);

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.get(WireMock
                        .urlMatching("/boards/%d/tags/%d"
                                .formatted(BOARD_ID, tagId)))
                .willReturn(
                        WireMock.aResponse().withHeader("Content-Type", "application/json").withBody(expectedResBody)));

        // Call the method being tested
        Tag returnedTag = this.serverUtils.getTag(tagId);

        // Verify the result
        Assertions.assertEquals(tagId, returnedTag.getId(),
                "The tag from the server should match the tag returned from the method.");
        this.wireMockServer.verify(1,
                WireMock.getRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/tags/%d"
                                .formatted(BOARD_ID, tagId))));
    }

    @Test
    void getAllTagsTest() throws Exception {
        this.setupServerAndConnection();

        final Tag tagToAdd1 = new Tag();
        final String tagTitle = "New Title";
        final String fontColor = "";
        final String backgroundColor = "";
        final long tagId = 20;

        final Tag tagToAdd2 = new Tag();
        final String tagTitle2 = "Brand New Title";
        final String fontColor2 = "";
        final String backgroundColor2 = "";
        final long tagId2 = 21;

        // Stub the server response
        this.wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/boards/%d/tags".formatted(BOARD_ID)))
                        .willReturn(WireMock.okJson(
                                ("{\"id\": %d, \"name\": \"%s\", \"fontColor\": \"%s\", \"backgroundColor\": \"%s\"," +
                                        " \"boardId\": \"%d\"}").formatted(
                                        tagId, tagTitle, fontColor, backgroundColor, BOARD_ID))));

        // Call the to add tag
        Tag addedTag1 = this.serverUtils.newTag(tagToAdd1);

        // Verify the server request
        this.wireMockServer.verify(1,
                WireMock.postRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/tags".formatted(BOARD_ID))));

        // Stub the server response
        this.wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/boards/%d/tags".formatted(BOARD_ID)))
                        .willReturn(WireMock.okJson(
                                ("{\"id\": %d, \"name\": \"%s\", \"fontColor\": \"%s\", \"backgroundColor\": \"%s\"," +
                                        " \"boardId\": \"%d\"}").formatted(
                                        tagId2, tagTitle2, fontColor2, backgroundColor2, BOARD_ID))));

        // Call the method to add tag
        Tag addedTag2 = this.serverUtils.newTag(tagToAdd2);

        // Verify the server request
        this.wireMockServer.verify(2, WireMock.postRequestedFor(WireMock
                .urlEqualTo("/boards/%d/tags".formatted(BOARD_ID))));

        List<Tag> createdTags = new ArrayList<>();
        createdTags.add(addedTag1);
        createdTags.add(addedTag2);

        this.wireMockServer.stubFor(
                WireMock.get(WireMock.urlEqualTo("/boards/%d/tags".formatted(BOARD_ID))).willReturn(
                WireMock.aResponse().withHeader("Content-Type", "application/json")
                        .withBody(("[{\"id\":%d,\"name\":\"%s\",\"fontColor\":\"%s\"," +
                                "\"backgroundColor\":\"%s\",\"boardId\": \"%d\"}," +
                                "{\"id\":%d,\"name\":\"%s\",\"fontColor\":\"%s\"," +
                                "\"backgroundColor\":\"%s\",\"boardId\": \"%d\"}]")
                                .formatted(tagId, tagTitle, fontColor, backgroundColor,
                                        BOARD_ID, tagId2, tagTitle2, fontColor2, backgroundColor2, BOARD_ID))));

        // Call the method being tested
        List<Tag> tags = this.serverUtils.getAllTags();

        // Verify the server request
        this.wireMockServer.verify(1, WireMock
                .getRequestedFor(WireMock.urlEqualTo("/boards/%d/tags".formatted(BOARD_ID))));

        // Verify the returned tag list.
        Assertions.assertEquals(createdTags, tags,
                "The returned tags should be the same one we have already added.");
    }

    @Test
    void deleteTagTest() throws Exception {
        this.setupServerAndConnection();

        final Tag tagToAdd = new Tag();
        final String tagTitle = "New Title";
        final String fontColor = "";
        final String backgroundColor = "";
        final long tagId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/boards/%d/tags".formatted(BOARD_ID)))
                        .willReturn(WireMock.okJson(
                                ("{\"id\": %d, \"name\": \"%s\", \"fontColor\": \"%s\", " +
                                        "\"backgroundColor\": \"%s\"," +
                                        " \"boardId\": \"%d\"}").formatted(
                                        tagId, tagTitle, fontColor, backgroundColor, BOARD_ID))));

        // Call the to add tag
        Tag addedTag = this.serverUtils.newTag(tagToAdd);

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching("/boards/%d/tags/%d"
                        .formatted(BOARD_ID, tagId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.deleteTag(addedTag);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.deleteRequestedFor(WireMock.urlEqualTo("/boards/%d/tags/%d"
                        .formatted(BOARD_ID, tagId))));
    }

    @Test
    void deleteTagThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long tagId = 0L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.delete(WireMock.urlMatching("/boards/%d/tags/%d"
                        .formatted(BOARD_ID, tagId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.deleteTag(new Tag());
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.deleteRequestedFor(WireMock.urlEqualTo("/boards/%d/tags/%d"
                        .formatted(BOARD_ID, tagId))));
    }

    @Test
    void addTagTest() throws Exception {
        this.setupServerAndConnection();

        final Tag tagToAdd = new Tag();
        final String tagTitle = "New Title";
        final String fontColor = "";
        final String backgroundColor = "";
        final long tagId = 20;
        final long listId = 1L;
        final long cardId = 1L;

        // Stub the server response
        String path = ("{\"id\": %d, \"name\": \"%s\", " +
                "\"fontColor\": \"%s\", \"backgroundColor\": \"%s\"," +
                " \"boardId\": \"%d\"}").formatted(
                tagId, tagTitle, fontColor, backgroundColor, BOARD_ID);
        this.wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/boards/%d/tags".formatted(BOARD_ID)))
                        .willReturn(WireMock.okJson(
                                path)));

        // Call the method being tested
        Tag addedTag = this.serverUtils.newTag(tagToAdd);

        // Verify the server request
        this.wireMockServer.verify(1, WireMock.postRequestedFor(WireMock
                .urlEqualTo("/boards/%d/tags".formatted(BOARD_ID))));

        // Stub the server response
        this.wireMockServer.stubFor(
                WireMock.put(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/tags/+%d"
                                .formatted(BOARD_ID, listId, cardId, tagId)))
                        .willReturn(WireMock.okJson(
                                path)));

        // Call the method being tested
        this.serverUtils.addTag(listId, cardId, addedTag);

        // Verify the server request
        this.wireMockServer.verify(1, WireMock.putRequestedFor(WireMock
                .urlEqualTo("/boards/%d/lists/%d/cards/%d/tags/+%d"
                .formatted(BOARD_ID, listId, cardId, tagId))));

        // Verify the returned tag.
        Assertions.assertEquals(tagId, addedTag.getId(),
                "The returned card ID should be correct.");
        Assertions.assertEquals(tagTitle, addedTag.getName(),
                "The returned tag name should be correct.");
        Assertions.assertEquals(fontColor, addedTag.getFontColor(),
                "The returned tag font color should be correct.");
        Assertions.assertEquals(backgroundColor, addedTag.getBackgroundColor(),
                "The returned tag background color should be correct.");
    }

    @Test
    void addTagThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        final long tagId = 0L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/lists/%d/cards/%d/tags/+%d"
                                .formatted(BOARD_ID, listId, cardId, tagId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.addTag(listId, cardId, new Tag());
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/lists/%d/cards/%d/tags/+%d"
                                .formatted(BOARD_ID, listId, cardId, tagId))));
    }

    @Test
    void removeTagTest() throws Exception {
        this.setupServerAndConnection();

        final Tag tag = new Tag();
        final String tagTitle = "New Title";
        final String fontColor = "";
        final String backgroundColor = "";

        final long listId = 1L;
        final long cardId = 2L;
        final long tagId = 3L;

        // Stub the server response
        String path = ("{\"id\": %d, \"name\": \"%s\", \"fontColor\": \"%s\", " +
                "\"backgroundColor\": \"%s\"," +
                " \"boardId\": \"%d\"}").formatted(
                tagId, tagTitle, fontColor, backgroundColor, BOARD_ID);
        this.wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/boards/%d/tags".formatted(BOARD_ID)))
                        .willReturn(WireMock.okJson(
                                path)));

        // Call the method being tested
        Tag newTag = this.serverUtils.newTag(tag);

        // Verify the result
        this.wireMockServer.verify(1, WireMock.postRequestedFor(WireMock
                .urlEqualTo("/boards/%d/tags".formatted(BOARD_ID))));

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/tags/-%d"
                .formatted(BOARD_ID, listId, cardId, tagId))).willReturn(WireMock.okJson(path)));

        // Call the method being tested
        this.serverUtils.removeTag(listId, cardId, newTag);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/tags/-%d"
                        .formatted(BOARD_ID, listId, cardId, tagId))));
    }

    @Test
    void removeTagThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        final long tagId = 0L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/lists/%d/cards/%d/tags/-%d"
                                .formatted(BOARD_ID, listId, cardId, tagId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.removeTag(listId, cardId, new Tag());
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/lists/%d/cards/%d/tags/-%d"
                                .formatted(BOARD_ID, listId, cardId, tagId))));
    }


    @Test
    void newSubTaskTest() throws Exception {
        this.setupServerAndConnection();

        final CardSubtask subtask = new CardSubtask();
        final String subtaskName = "New Task";
        final boolean completed = false;
        final long listId = 1L;
        final long cardId = 1L;
        final long subtaskId = 2L;

        // Stub the server response
        String path = ("{\"id\": %d, \"name\": \"%s\", \"completed\": \"%s\"," +
                " \"card\": \"%d\"}").formatted(subtaskId, subtaskName, completed, cardId);
        this.wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks"
                                .formatted(BOARD_ID, listId, cardId)))
                        .willReturn(WireMock.okJson(path)));

        // Call the method being tested
        CardSubtask addedSubTask = this.serverUtils.newSubTask(listId, cardId, subtask);

        // Verify the server request
        this.wireMockServer.verify(1, WireMock.postRequestedFor(WireMock
                .urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks"
                        .formatted(BOARD_ID, listId, cardId))));

        // Verify the added subtask.
        Assertions.assertEquals(subtaskId, addedSubTask.getId());
        Assertions.assertEquals(completed, addedSubTask.isCompleted());
        Assertions.assertEquals(cardId, addedSubTask.getCard());
    }

    @Test
    void removeSubTaskTest() throws Exception {
        this.setupServerAndConnection();

        final CardSubtask subtask = new CardSubtask();
        final String subtaskName = "New Task";
        final boolean completed = false;
        final long listId = 1L;
        final long cardId = 1L;
        final long subtaskId = 2L;

        // Stub the server response
        String path = ("{\"id\": %d, \"name\": \"%s\", \"completed\": \"%s\"," +
                " \"card\": \"%d\"}").formatted(subtaskId, subtaskName, completed, cardId);

        this.wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks"
                                .formatted(BOARD_ID, listId, cardId)))
                        .willReturn(WireMock.okJson(path)));

        // Call the method being tested
        CardSubtask addedSubTask = this.serverUtils.newSubTask(listId, cardId, subtask);

        // Verify the server request
        this.wireMockServer.verify(1, WireMock.postRequestedFor(WireMock
                .urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks"
                        .formatted(BOARD_ID, listId, cardId))));

        this.wireMockServer.stubFor(
                WireMock.delete(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks/%d"
                                .formatted(BOARD_ID, listId, cardId, subtaskId)))
                        .willReturn(WireMock.okJson(path)));

        // Call the method being tested
        this.serverUtils.removeSubTask(listId, cardId, addedSubTask);

        // Verify the server request
        this.wireMockServer.verify(1, WireMock.deleteRequestedFor(WireMock
                .urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks/%d"
                        .formatted(BOARD_ID, listId, cardId, subtaskId))));
    }

    @Test
    void removeSubTaskThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        final long subtaskId = 0L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.delete(WireMock
                        .urlMatching("/boards/%d/lists/%d/cards/%d/subtasks/%d"
                                .formatted(BOARD_ID, listId, cardId, subtaskId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.removeSubTask(listId, cardId, new CardSubtask());
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.deleteRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks/%d"
                                .formatted(BOARD_ID, listId, cardId, subtaskId))));
    }

    @Test
    void setSubtaskCompletenessTest() throws Exception {
        this.setupServerAndConnection();

        final CardSubtask subtask = new CardSubtask();
        final String subtaskName = "New Task";
        final boolean completed = true;
        final long listId = 1L;
        final long cardId = 1L;
        final long subtaskId = 2L;

        // Stub the server response
        String path = ("{\"id\": %d, \"name\": \"%s\", \"completed\": \"%s\"," +
                " \"card\": \"%d\"}").formatted(subtaskId, subtaskName, completed, cardId);

        this.wireMockServer.stubFor(
                WireMock.post(WireMock.urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks"
                                .formatted(BOARD_ID, listId, cardId)))
                        .willReturn(WireMock.okJson(path)));

        // Call the method being tested
        CardSubtask addedSubTask = this.serverUtils.newSubTask(listId, cardId, subtask);

        // Verify the server request
        this.wireMockServer.verify(1, WireMock.postRequestedFor(WireMock
                .urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks"
                        .formatted(BOARD_ID, listId, cardId))));
        boolean newCompleteness = true;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/lists/%d/cards/%d/subtasks/%d/completeness"
                                .formatted(BOARD_ID, listId, cardId, subtaskId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.setSubtaskCompleteness(listId, cardId, addedSubTask, newCompleteness);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks/%d/completeness"
                        .formatted(BOARD_ID, listId, cardId, subtaskId))));

        Assertions.assertTrue(addedSubTask.isCompleted());
    }

    @Test
    void setSubtaskCompletenessThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long listId = 1L;
        final long cardId = 1L;
        final long subtaskId = 0L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/lists/%d/cards/%d/subtasks/%d/completeness"
                                .formatted(BOARD_ID, listId, cardId, subtaskId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.setSubtaskCompleteness(listId, cardId, new CardSubtask(), true);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/lists/%d/cards/%d/subtasks/%d/completeness"
                                .formatted(BOARD_ID, listId, cardId, subtaskId))));
    }


    @Test
    void setBoardBackgroundColorTest() throws Exception {
        this.setupServerAndConnection();

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/backgroundColor".formatted(BOARD_ID)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.setBoardBackgroundColor("testBoardBackgroundColor");

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/backgroundColor".formatted(BOARD_ID))));
    }

    @Test
    void setBoardBackgroundColorThrowsTest() throws Exception {
        this.setupServerAndConnection();

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/backgroundColor".formatted(BOARD_ID)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.setBoardBackgroundColor("testBoardBackgroundColor");
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/backgroundColor".formatted(BOARD_ID))));
    }

    @Test
    void setBoardFontColorTest() throws Exception {
        this.setupServerAndConnection();

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/fontColor".formatted(BOARD_ID)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.setBoardFontColor("testBoardFontColor");

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/fontColor".formatted(BOARD_ID))));
    }

    @Test
    void setBoardFontColorThrowsTest() throws Exception {
        this.setupServerAndConnection();

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/fontColor".formatted(BOARD_ID)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.setBoardFontColor("testBoardFontColor");
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/fontColor".formatted(BOARD_ID))));
    }

    @Test
    void setCardListBackgroundColorTest() throws Exception {
        this.setupServerAndConnection();

        final long listId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/lists/%d/backgroundColor".formatted(BOARD_ID, listId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.setCardListBackgroundColor(listId, "testListBackgroundColor");


        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/lists/%d/backgroundColor".formatted(BOARD_ID, 1L))));
    }

    @Test
    void setCardListBackgroundColorThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long listId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/lists/%d/backgroundColor".formatted(BOARD_ID, listId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.setCardListBackgroundColor(listId, "testListBackgroundColor");
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/lists/%d/backgroundColor".formatted(BOARD_ID, 1L))));
    }

    @Test
    void setCardListFontColorTest() throws Exception {
        this.setupServerAndConnection();

        final long listId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/lists/%d/fontColor".formatted(BOARD_ID, listId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.setCardListFontColor(listId, "testListFontColor");


        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/lists/%d/fontColor".formatted(BOARD_ID, 1L))));
    }

    @Test
    void setCardListFontColorThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long listId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/lists/%d/fontColor".formatted(BOARD_ID, listId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.setCardListFontColor(listId, "testListFontColor");
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/lists/%d/fontColor".formatted(BOARD_ID, 1L))));
    }

    @Test
    void setTagBackgroundColorTest() throws Exception {
        this.setupServerAndConnection();

        final long tagId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/tags/%d/tag_background_color".formatted(BOARD_ID, tagId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.changeTagBackgroundColor(tagId, "testTagBackgroundColor");


        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/tags/%d/tag_background_color".formatted(BOARD_ID, 1L))));
    }

    @Test
    void setTagBackgroundColorThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long tagId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/tags/%d/tag_background_color".formatted(BOARD_ID, tagId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.changeTagBackgroundColor(tagId, "testTagBackgroundColor");
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/tags/%d/tag_background_color".formatted(BOARD_ID, 1L))));
    }

    @Test
    void setTagFontColorTest() throws Exception {
        this.setupServerAndConnection();

        final long tagId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/tags/%d/tag_color".formatted(BOARD_ID, tagId)))
                .willReturn(
                        WireMock.aResponse().withBody(WireMock.ok().build().getBody())));

        // Call the method being tested
        this.serverUtils.changeTagFontColor(tagId, "testTagFontColor");


        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/tags/%d/tag_color".formatted(BOARD_ID, tagId))));
    }

    @Test
    void setTagFontColorThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final long tagId = 1L;

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/tags/%d/tag_color".formatted(BOARD_ID, tagId)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.changeTagFontColor(tagId, "testTagFontColor");
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/tags/%d/tag_color".formatted(BOARD_ID, tagId))));
    }

    @Test
    void setBoardTitleTest() throws Exception {
        this.setupServerAndConnection();

        final String newTitle = "new title";

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/board_title".formatted(BOARD_ID)))
                .willReturn(
                        WireMock.aResponse().withBody(newTitle)));

        // Call the method being tested
        this.serverUtils.setBoardTitle(BOARD_ID, newTitle);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/board_title".formatted(BOARD_ID))));
    }

    @Test
    void setBoardTitleThrowsTest() throws Exception {
        this.setupServerAndConnection();

        final String newTitle = "new title";

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.put(WireMock
                        .urlMatching("/boards/%d/board_title".formatted(BOARD_ID)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.setBoardTitle(BOARD_ID, newTitle);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.putRequestedFor(WireMock
                        .urlEqualTo("/boards/%d/board_title".formatted(BOARD_ID))));
    }

    @Test
    void deleteBoardTest() throws Exception {
        this.setupServerAndConnection();

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.delete(WireMock
                        .urlMatching("/boards/%d".formatted(BOARD_ID)))
                .willReturn(
                        WireMock.aResponse()));

        // Call the method being tested
        this.serverUtils.deleteBoard(BOARD_ID);

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.deleteRequestedFor(WireMock
                        .urlEqualTo("/boards/%d".formatted(BOARD_ID))));
    }

    @Test
    void deleteBoardThrowsTest() throws Exception {
        this.setupServerAndConnection();

        // Stub the server response
        this.wireMockServer.stubFor(WireMock.delete(WireMock
                        .urlMatching("/boards/%d".formatted(BOARD_ID)))
                .willReturn(
                        WireMock.aResponse().withStatus(404)));

        // Call the method being tested
        Assertions.assertThrows(NotFoundException.class, () -> {
            this.serverUtils.deleteBoard(BOARD_ID);
        });

        // Verify the result
        this.wireMockServer.verify(1,
                WireMock.deleteRequestedFor(WireMock
                        .urlEqualTo("/boards/%d".formatted(BOARD_ID))));
    }

    private class MyTestModule extends AbstractModule {
        /**
         * Configures the module.
         */
        @Override
        protected void configure() {
            this.bind(ServerUtilsInterface.class).to(ServerUtils.class).in(Scopes.SINGLETON);
            this.bind(WebsocketClientEndpoint.class).toProvider(() -> ServerUtilsTest.this.mockWsClient);
        }
    }
}
