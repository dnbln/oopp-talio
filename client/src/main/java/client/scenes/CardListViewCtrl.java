package client.scenes;

import client.scenes.helperclasses.CardListListView;
import client.utils.NoListThereException;
import client.utils.ServerUtilsInterface;
import com.google.inject.Inject;
import commons.Card;
import commons.CardList;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

/**
 * The controller of the CardListView scene. This is used to make a ListView of a CardList.
 */
public class CardListViewCtrl {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ServerUtilsInterface server;
    private final MainCtrl mainCtrl;
    private final BoardOverviewCtrl boardOverviewCtrl;

    @FXML
    private VBox theBox;

    @FXML
    private TextField listTitle;

    @FXML
    private Button addButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button changeColorButton;

    @FXML
    private Button deleteButton;

    private CardList cardList;
    private ListView<Card> cardListView;

    /**
     * Constructor for the CardListViewCtrl. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param server            The connection to the server.
     * @param mainCtrl          The connection to the main controller.
     * @param boardOverviewCtrl The connection to the board overview controller.
     */
    @Inject
    public CardListViewCtrl(final ServerUtilsInterface server, final MainCtrl mainCtrl,
                            final BoardOverviewCtrl boardOverviewCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.boardOverviewCtrl = boardOverviewCtrl;
    }

    /**
     * Initialize the card list view.
     *
     * @param cardListForInit the CardList to make the ListView of.
     */
    public void initData(final CardList cardListForInit) {
        this.cardList = cardListForInit;
        this.listTitle.setText(this.cardList.getTitle());
        this.listTitle.getStylesheets().add("client/scenes/CardListView.css");

        if (!"".equals(this.cardList.getBackgroundColor())) {
            this.applyCustomize(this.cardList.getId());
        }
        ListView<Card> cardListListView = new CardListListView(this.mainCtrl, this.server, this.cardList);
        cardListListView.setItems(FXCollections.observableArrayList(this.cardList.getCards()));

        if (!this.cardList.getBackgroundColor().isEmpty()) {
            this.applyCustomize(this.cardList.getId());
        }
        this.cardListView = new CardListListView(this.mainCtrl, this.server, this.cardList);
        this.cardListView.setItems(FXCollections.observableArrayList(this.cardList.getCards()));

        this.theBox.getChildren().add(1, this.cardListView);

        this.addButton.setOnAction(e -> this.showCardDetails(this.cardList.getId()));
        this.changeColorButton.setOnAction(e -> this.showCustomizeList(this.cardList.getId(), this));
        this.settingsButton.setOnAction(e -> this.showListSettings());
        this.deleteButton.setOnAction(e -> this.server.deleteList(this.cardList.getId()));
        this.listTitle.setOnKeyPressed(event -> { // Remove focus when Enter is pressed
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.ESCAPE) {
                this.listTitle.getParent().requestFocus();
            }
        });
        this.listTitle.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.booleanValue()) { // When the list title field loses focus
                this.updateListTitle();
            }
        });

        this.cardListView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                if (!this.cardListView.getItems().isEmpty()) {
                    this.cardListView.getSelectionModel().select(0);
                }
            } else { // When the list field loses focus
                this.cardListView.getSelectionModel().clearSelection();
            }
        });

        this.addButton.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                this.addButton.setStyle("-fx-border-color: #47adff;");
            } else {
                this.addButton.setStyle("-fx-background-color: null;");
            }
        });
        this.changeColorButton.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                this.changeColorButton.setStyle("-fx-border-color: #47adff;");
            } else {
                this.changeColorButton.setStyle("-fx-background-color: null;");
            }
        });
        this.settingsButton.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                this.settingsButton.setStyle("-fx-border-color: #47adff;");
            } else {
                this.settingsButton.setStyle("-fx-background-color: null;");
            }
        });
        this.deleteButton.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.booleanValue()) {
                this.deleteButton.setStyle("-fx-border-color: #47adff;");
            } else {
                this.deleteButton.setStyle("-fx-background-color: null;");
            }
        });
        this.deleteButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.server.deleteList(this.cardList.getId());
            }
        });
        this.addButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.showCardDetails(this.cardList.getId());
            }
        });
        this.settingsButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.showListSettings();
            }
        });
        this.changeColorButton.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                this.showCustomizeList(this.cardList.getId(), this);
            }
        });

        this.cardListView.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
    }


    private void showCardDetails(final long listId) {
        this.mainCtrl.showCardDetails(listId);
    }

    private void showListSettings() {
        // Create a new TextInputDialog with no initial text
        TextInputDialog dialog = new TextInputDialog(this.listTitle.getText());

        // Set the title and header text of the dialog
        dialog.setTitle("List Settings");
        dialog.setHeaderText("Please enter the new title of this list");

        // Show the dialog and wait for the user's response
        Optional<String> result = dialog.showAndWait();

        // If the user entered a text, update the list title
        result.ifPresent(title -> this.server.setListTitle(this.cardList.getId(), title));
    }

    private void updateListTitle() {
        String text = this.listTitle.getText();
        if (text.trim().isEmpty()) {
            this.listTitle.setText(this.cardList.getTitle());
        } else if (!text.equals(this.cardList.getTitle())) {
            this.server.setListTitle(this.cardList.getId(), this.listTitle.getText());
        }
    }

    /**
     * Handle key pressed in this scene.
     *
     * @param keyEvent the key event.
     */
    public void handleKeyPressed(final javafx.scene.input.KeyEvent keyEvent) {
        if ("?".equals(keyEvent.getText()) || keyEvent.getCode() == KeyCode.H || keyEvent.getCode() == KeyCode.SLASH) {
            //Show help screen.
            this.mainCtrl.showHelpAlert();
        } else if (keyEvent.getCode() == KeyCode.E) { //Edit the card title in overview.
            Card selectedCard = this.cardListView.getSelectionModel().getSelectedItem();
            this.changeCardNameDirectly(selectedCard);
        } else if (keyEvent.getCode() == KeyCode.N || keyEvent.getCode() == KeyCode.ENTER) { //Show card details.
            if (!this.cardListView.getSelectionModel().getSelectedItems().isEmpty()) {
                this.mainCtrl.showCardDetails(this.cardListView.getSelectionModel().getSelectedItems().get(0));
            }
        } else if (keyEvent.getCode() == KeyCode.DELETE || keyEvent.getCode() == KeyCode.BACK_SPACE) { //Delete card.
            if (!this.cardListView.getSelectionModel().getSelectedItems().isEmpty()) {
                this.server.deleteCard(this.cardList.getId(),
                        this.cardListView.getSelectionModel().getSelectedItems().get(0).getId());
                this.mainCtrl.showOverview();
            }
        } else if (keyEvent.isShiftDown() && (keyEvent.getCode() == KeyCode.U || keyEvent.getCode() == KeyCode.UP)) {
            //Move card up.
            int selectedIndex = this.cardListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex > 0) {
                this.switchWithCardBefore(selectedIndex);

            }
        } else if (keyEvent.isShiftDown() && (keyEvent.getCode() == KeyCode.D || keyEvent.getCode() == KeyCode.DOWN)) {
            //Move card down.
            int selectedIndex = this.cardListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                this.switchWithCardAfter(selectedIndex);
            }
        } else if (keyEvent.isShiftDown() && (keyEvent.getCode() == KeyCode.L || keyEvent.getCode() == KeyCode.LEFT)) {
            //Move card left.
            int selectedIndex = this.cardListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                this.moveToOtherList(selectedIndex, MoveDirection.LEFT);
            }
        } else if (keyEvent.isShiftDown() && (keyEvent.getCode() == KeyCode.R || keyEvent.getCode() == KeyCode.RIGHT)) {
            //Move card right
            int selectedIndex = this.cardListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                this.moveToOtherList(selectedIndex, MoveDirection.RIGHT);
            }
        } else {
            return;
        }
        keyEvent.consume();
    }

    /**
     * Shows a pop-up to change the name of the selected card.
     *
     * @param selectedCard the selected card.
     */
    public void changeCardNameDirectly(final Card selectedCard) {
        if (selectedCard != null) {
            TextInputDialog dialog = new TextInputDialog(selectedCard.getTitle());
            dialog.setTitle("Edit Card Title");
            dialog.setHeaderText(null);
            dialog.setContentText("Enter a new title for the card:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(
                    newTitle -> this.server.setCardTitle(this.cardList.getId(), selectedCard.getId(), newTitle));
        }
    }

    private void moveToOtherList(final int indexOfSelectedCard, final int moveDirection) {
        Card selectedCard = this.cardListView.getItems().get(indexOfSelectedCard);
        try {
            CardList leftList;
            if (moveDirection == MoveDirection.LEFT) {
                leftList = this.boardOverviewCtrl.getLeftList(this.cardList);
            } else if (moveDirection == MoveDirection.RIGHT) {
                leftList = this.boardOverviewCtrl.getRightList(this.cardList);
            } else {
                throw new IllegalArgumentException("moveDirection must be either LEFT or RIGHT");
            }
            if (leftList.getCards().isEmpty()) {
                this.server.xListCardMove(this.cardList.getId(), selectedCard.getId(), leftList.getId(), 0);
                return;
            }
            this.server.xListCardMove(this.cardList.getId(), selectedCard.getId(), leftList.getId(),
                    leftList.getCards().get(leftList.getCards().size() - 1).getId());
        } catch (final NoListThereException e) {
            // Do nothing
        }
    }

    /**
     * Switches a card with the card before it in the list.
     * To be more precise, it places the selected card after the second previous card.
     * If there is no second previous card, it places the selected card at the beginning of the list.
     * If the selected card is the first card in the list, nothing happens.
     * <p>
     * This is done because this way, the selected card will be the card that receives the focus after the switch.
     *
     * @param indexOfSelectedCard the index of the selected card.
     */
    public void switchWithCardBefore(final int indexOfSelectedCard) {
        Card selectedCard = this.cardListView.getItems().get(indexOfSelectedCard);
        int indexOf2Previous = indexOfSelectedCard - 2;
        if (indexOf2Previous == -1) {
            this.server.sameListCardMove(this.cardList.getId(), selectedCard.getId(), 0);
            return;
        } else if (indexOf2Previous < -1) {
            return;
        }
        Card previousCard = this.cardListView.getItems().get(indexOf2Previous);
        if (indexOfSelectedCard < this.cardListView.getItems().size()) {
            this.server.sameListCardMove(this.cardList.getId(), selectedCard.getId(), previousCard.getId());
        }
    }

    /**
     * Switches a card with the card after it in the list.
     * To be more precise, it places the selected card after the next card.
     * If the selected card is the last card in the list, nothing happens.
     *
     * @param indexOfSelectedCard the index of the selected card.
     */
    public void switchWithCardAfter(final int indexOfSelectedCard) {
        Card selectedCard = this.cardListView.getItems().get(indexOfSelectedCard);
        LOGGER.info("in switch after");

        int indexOfNext = indexOfSelectedCard + 1;
        if (indexOfNext >= this.cardListView.getItems().size()) {
            return;
        }
        Card nextCard = this.cardListView.getItems().get(indexOfNext);
        LOGGER.info(indexOfSelectedCard);
        this.server.sameListCardMove(this.cardList.getId(), selectedCard.getId(), nextCard.getId());
    }

    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    private enum MoveDirection {
        ;
        public static final int LEFT = 0;
        public static final int RIGHT = 1;

    }

    /**
     * Shows the customization lists scene.
     *
     * @param listId The id of the list.
     * @param ctrl   controller of the list view.
     */
    private void showCustomizeList(final long listId, final CardListViewCtrl ctrl) {
        this.mainCtrl.showListCustomize(listId, ctrl);
    }

    /**
     * Apply the changes from the customization scene.
     *
     * @param listId The id of the list.
     */
    public void applyCustomize(final long listId) {
        this.listTitle.setStyle("-fx-text-fill: %s".formatted(this.server.getList(listId).getFontColor()));
        this.theBox.setStyle("-fx-background-color: %s; -fx-border-color: black".formatted(
                this.server.getList(listId).getBackgroundColor()));
    }

    /**
     * Reset the changes from the customization scene.
     */
    public void resetCustomize() {
        this.listTitle.setStyle("-fx-text-fill: %s".formatted("#000000"));
        this.theBox.setStyle("-fx-background-color: %s; -fx-border-color: black".formatted("#ffffff"));
    }
}
