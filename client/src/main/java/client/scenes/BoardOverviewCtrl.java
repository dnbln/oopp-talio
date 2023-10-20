package client.scenes;

import client.scenes.helperclasses.CardListListView;
import client.utils.NoListThereException;
import client.utils.ServerUtilsInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import commons.CardList;
import commons.Tag;
import commons.events.CardMovedEvent;
import commons.events.ServerToClientEvent;
import commons.events.XListCardMoveEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

/**
 * The board overview controller.
 */
public class BoardOverviewCtrl {
    private static final String DEFAULT_LIST_TITLE = "My Task List";
    private final ServerUtilsInterface server;
    private final MainCtrl mainCtrl;

    @FXML
    private HBox lists;

    @FXML
    private Button customizeButton;

    @FXML
    private Button tagMenuButton;

    @FXML
    private Button disconnectButton;

    @FXML
    private Button workSpaceButton;

    @FXML
    private StackPane stackPane;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Label boardTitle;

    @FXML
    private Button addListButton;

    /**
     * Constructor for the BoardOverviewCtrl. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param server   The connection to the server.
     * @param mainCtrl The connection to the main controller.
     */
    @Inject
    public BoardOverviewCtrl(final ServerUtilsInterface server, final MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    /**
     * Call to initialize a controller.
     *
     * @param event The event that triggered this method.
     */
    public void reloadBoard(final ServerToClientEvent event) {
        this.boardTitle.setText(this.server.getBoard().getTitle());

        this.customizeButton.setOnAction(e -> this.showCustomizeMenu(this));

        ObservableList<Node> listChildren = this.lists.getChildren();
        Node saveAddListButton = listChildren.get(listChildren.size() - 1);
        this.lists.getChildren().clear();
        List<CardList> cardLists = this.server.getBoard().getCardLists();
        for (final CardList cardList : cardLists) {
            // Load the CardListView from the FXML file
            Pair<? extends CardListViewCtrl, ? extends Parent> cardListViewPair = this.mainCtrl.getCardListViewPair();
            VBox listContainer = (VBox) cardListViewPair.getValue();
            CardListViewCtrl cardListViewCtrl = cardListViewPair.getKey();
            cardListViewCtrl.initData(cardList);

            listChildren.add(listContainer);
        }
        listChildren.add(saveAddListButton);

        this.setFocus(event);

        this.addListButton.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                this.addListButton.setStyle("-fx-border-color: #47adff;");
            } else {
                this.addListButton.setStyle("-fx-background-color: null;");
            }
        });
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    void setFocus(final ServerToClientEvent event) {
        if (event instanceof final CardMovedEvent cardMovedEvent) {
            long cardId = cardMovedEvent.card();
            long listId = cardMovedEvent.cardListId();
            this.setFocus(cardId, listId);
        } else if (event instanceof final XListCardMoveEvent xListCardMoveEvent) {
            long cardId = xListCardMoveEvent.card();
            long listId = xListCardMoveEvent.destCardList();
            this.setFocus(cardId, listId);
        }
    }

    private void setFocus(final long cardId, final long listId) {
        ObservableList<Node> listChildren = this.lists.getChildren();
        for (final Node node : listChildren) {
            if (node instanceof final VBox vbox) {
                ObservableList<Node> vboxChildren = vbox.getChildren();
                for (final Node child : vboxChildren) {
                    if (child instanceof final CardListListView cardListListView &&
                        cardListListView.getListId() == listId) {
                        cardListListView.setFocus(cardId);
                    }
                }
            }
        }
    }

    /**
     * Apply the changes from the customization scene.
     */
    public void applyCustomize() {
        this.boardTitle.setStyle("-fx-text-fill: %s".formatted(this.server.getBoard().getFontColor()));
        this.disconnectButton.setStyle("-fx-text-fill: %s".formatted(this.server.getBoard().getFontColor()));
        this.tagMenuButton.setStyle("-fx-text-fill: %s".formatted(this.server.getBoard().getFontColor()));
        this.workSpaceButton.setStyle("-fx-text-fill: %s".formatted(this.server.getBoard().getFontColor()));
        this.customizeButton.setStyle("-fx-text-fill: %s".formatted(this.server.getBoard().getFontColor()));

        this.stackPane.setStyle("-fx-background-color: %s; -fx-border-color: black".formatted(this.server.getBoard()
                .getBackgroundColor()));
        this.lists.setStyle("-fx-background-color: %s".formatted(this.server.getBoard().getBackgroundColor()));
        this.scrollPane.setStyle("-fx-background-color: %s".formatted(this.server.getBoard().getBackgroundColor()));
    }

    /**
     * Reset the changes from the customization scene.
     */
    public void resetCustomize() {
        this.boardTitle.setStyle("-fx-text-fill: %s".formatted("#000000"));
        this.disconnectButton.setStyle("-fx-text-fill: %s".formatted("#000000"));
        this.tagMenuButton.setStyle("-fx-text-fill: %s".formatted("#000000"));
        this.workSpaceButton.setStyle("-fx-text-fill: %s".formatted("#000000"));
        this.customizeButton.setStyle("-fx-text-fill: %s".formatted("#000000"));

        this.stackPane.setStyle("-fx-background-color: %s; -fx-border-color: black".formatted("#ffffff"));
        this.lists.setStyle("-fx-background-color: %s".formatted("#ffffff"));
        this.scrollPane.setStyle("-fx-background-color: %s".formatted("#ffffff"));
    }

    /**
     * Adds an empty list to the board.
     * Prompts the user for a title before creating.
     */
    public void addList() {
        CardList list = this.server.addList();
        this.server.setListTitle(list.getId(), DEFAULT_LIST_TITLE);
    }

    /**
     * Goes back to the Server Connect screen.
     * Alerts the user if they really want to exit and connect to another server.
     */
    public void backToServerConnect() {
        // Create a new Alert for going back to the server connect
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Back Confirmation");
        alert.setHeaderText("Are you sure you want to go back to the server connection?");

        // Set the button types for the Alert
        ButtonType yesButton = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        // Get the result of the Alert
        Optional<ButtonType> result = alert.showAndWait();

        result.ifPresent(buttonPressed -> {
            if (buttonPressed.equals(yesButton)) {
                try {
                    this.server.unsubscribe();
                } catch (final JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                this.mainCtrl.showConnect();
            }
        });
    }

    /**
     * Goes to the board list scene to change/add/delete boards.
     *
     * @throws JsonProcessingException an exception
     */
    public void changeBoard() throws JsonProcessingException {
        this.server.unsubscribe();
        this.mainCtrl.showBoardList();
    }

    /**
     * Handles the pressing of keys in this scene.
     *
     * @param keyEvent the event of pressing a key.
     * @throws JsonProcessingException an exception
     */
    public void handleKeyPressed(final javafx.scene.input.KeyEvent keyEvent) throws JsonProcessingException {
        if ("?".equals(keyEvent.getText()) || keyEvent.getCode() == KeyCode.H || keyEvent.getCode() == KeyCode.SLASH) {
            //Show help screen.
            mainCtrl.showHelpAlert();
            return;
        }
        if (keyEvent.getCode() == KeyCode.W) { //Go to the workspace.
            this.changeBoard();
            return;
        }
        if (keyEvent.getCode() == KeyCode.D) { //Go to the server connection.
            this.backToServerConnect();
            return;
        }
        if (keyEvent.getCode() == KeyCode.A) { //Add a new list.
            this.addList();
            return;
        }
        if (keyEvent.getCode() == KeyCode.T) { //Open the tag menu.
            this.showTagMenu();
            return;
        }
        if (keyEvent.getCode() == KeyCode.C) { //Open the customization menu.
            this.showCustomizeMenu(this);
        }
    }

    /**
     * Gets the list to the left of the given list.
     * If there is no list to the left, an IllegalArgumentException is thrown.
     *
     * @param cardList The list to get the list to the left of.
     * @return The list to the left of the given list.
     * @throws NoListThereException if there is no list to the left.
     */
    public CardList getLeftList(final CardList cardList) throws NoListThereException {
        final int currentIndex = this.getCardListIndex(cardList);
        if (currentIndex == -1 || currentIndex == 0) {
            throw new NoListThereException("There is no list to the left of this list.");
        }
        return this.server.getBoard().getCardLists().get(currentIndex - 1);
    }

    /**
     * Gets the list to the right of the given list.
     * If there is no list to the right, an IllegalArgumentException is thrown.
     *
     * @param cardList The list to get the list to the right of.
     * @return The list to the right of the given list.
     * @throws NoListThereException if there is no list to the right.
     */
    public CardList getRightList(final CardList cardList) throws NoListThereException {
        final int currentIndex = this.getCardListIndex(cardList);
        if (currentIndex == -1 || currentIndex == this.server.getBoard().getCardLists().size() - 1) {
            throw new NoListThereException("There is no list to the right of this list.");
        }
        return this.server.getBoard().getCardLists().get(currentIndex + 1);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    int getCardListIndex(final CardList cardList) {
        final ObservableList<Node> listChildren = this.lists.getChildren();
        int size = listChildren.size();
        for (int i = 0; i < size; i++) {
            final Node child = listChildren.get(i);
            if (child instanceof final VBox vbox) {
                final ObservableList<Node> vboxChildren = (vbox).getChildren();
                for (final Node vboxChild : vboxChildren) {
                    if (vboxChild instanceof final CardListListView cardListListView &&
                            (cardListListView).getListId() == cardList.getId()) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * shows the tag menu.
     *
     */
    public void showTagMenu() {
        List<Tag> tags = this.server.getAllTags();
        ObservableList<Tag> observableTags = FXCollections.observableArrayList(tags);
        this.mainCtrl.showAddTag(observableTags, false);
    }

    /**
     * shows the customization menu.
     *
     * @param boardOverviewCtrl the bordOverview Controller.
     */
    public void showCustomizeMenu(final BoardOverviewCtrl boardOverviewCtrl) {
        this.mainCtrl.showCustomize(boardOverviewCtrl);
    }
}


