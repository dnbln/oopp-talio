package client.scenes;

import client.scenes.helperclasses.SubTaskCell;
import client.scenes.helperclasses.TagCell;
import client.utils.ServerUtilsInterface;
import com.google.inject.Inject;
import commons.Card;
import commons.CardSubtask;
import commons.Tag;
import commons.events.CardRemovedEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.stage.Modality;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * The card details controller.
 */
public class CardDetailsCtrl implements Initializable {
    private final ServerUtilsInterface server;
    private final MainCtrl mainCtrl;
    private Card cardObject = new Card();

    @FXML
    private ListView<CardSubtask> subTasks;

    @FXML
    private Button deleteButton;

    @FXML
    private TextField taskName;

    @FXML
    private TextField taskDescription;

    @FXML
    private ListView<Tag> tags;

    private long listId;

    /**
     * Called when we receive a CardRemovedEvent.
     * @param e The CardRemovedEvent.
     */
    public void cardDeleted(final CardRemovedEvent e) {
        if (e.card().getId() != this.cardObject.getId()) {
            return;
        }

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Card deleted.");
        alert.setHeaderText(null);
        alert.setContentText("The card was deleted.");
        alert.showAndWait();

        showBoard();
    }

    /**
     * Constructor for the CardDetailsCtrl. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param server   The connection to the server.
     * @param mainCtrl The connection to the main controller.
     */
    @Inject
    public CardDetailsCtrl(final ServerUtilsInterface server, final MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        this.subTasks.setCellFactory(lv -> new SubTaskCell());
        this.tags.setCellFactory(lv -> {
            ListCell<Tag> tagCell = new TagCell();
            tagCell.setStyle("-fx-background-color: transparent;");
            return tagCell;
        });
    }

    @FXML
    private void addSubTasks() {
        TextInputDialog dialog = new TextInputDialog("New Sub-Task");

        // Set the title and header text of the dialog
        dialog.setTitle("Sub-Task Name");
        dialog.setHeaderText("Please enter a name for the sub-task");

        // Show the dialog and wait for the user's response
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (name.trim().isEmpty()) {
                alertMessage("You can't create a subtask without a name.");
                this.addSubTasks();
                return;
            }
            CardSubtask subtask = new CardSubtask();
            subtask.patch(subtaskPatcher -> subtaskPatcher.setName(name));
            this.subTasks.getItems().add(subtask);
        });
    }

    private static void alertMessage(final String contentText) {
        final Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    @FXML
    private void addTag() {
        this.mainCtrl.showAddTag(this.tags.getItems(), true);
    }

    /**
     * Change the scene to the board overview.
     */
    public void showBoard() {
        this.clearFields();
        this.mainCtrl.showOverview();
    }

    /**
     * Add a new card to the static card list.
     */
    public void apply() {
        if (this.taskName.getText().trim().isEmpty()) {
            alertMessage("You can't create a task without a title.");
        } else {
            if (this.cardObject.getId() == 0) {
                this.cardObject.patch(cardPatcher -> cardPatcher.setTitle(this.taskName.getText()));
                this.cardObject.patch(cardPatcher -> cardPatcher.setText(this.taskDescription.getText()));
                this.cardObject = this.server.addCard(this.listId, this.cardObject);
            } else {
                this.setTitle();
                this.setDescription();
            }
            this.setTags();
            this.setSubtasks();
            this.cardObject = new Card();
            this.showBoard();
        }
    }

    /**
     * Set the title if it is not the same as the original.
     */
    public void setTitle() {
        if (!this.cardObject.getTitle().equals(this.taskName.getText())) {
            this.server.setCardTitle(this.cardObject.getCardList(), this.cardObject.getId(), this.taskName.getText());
        }
    }

    /**
     * Set the description if it is not the same as the original.
     */
    public void setDescription() {
        if (!this.cardObject.getText().equals(this.taskDescription.getText())) {
            this.server.setCardText(this.cardObject.getCardList(), this.cardObject.getId(),
                    this.taskDescription.getText());
        }
    }

    /**
     * Set the subtasks if they are not the same as the original.
     */
    public void setSubtasks() {
        Collection<CardSubtask> unionOfSubtasks = new HashSet<>(this.cardObject.getSubtasks());
        unionOfSubtasks.addAll(this.subTasks.getItems());
        for (final CardSubtask subtask : unionOfSubtasks) {
            if (this.subTasks.getItems().contains(subtask)) {
                if (!this.cardObject.getSubtasks().contains(subtask)) {
                    this.server.newSubTask(this.cardObject.getCardList(), this.cardObject.getId(), subtask);
                }
                CheckBox checkBox = (CheckBox) this.subTasks.lookup("#%d".formatted(subtask.getId()));
                if (checkBox.isSelected() != subtask.isCompleted()) {
                    this.updateSubtaskCompleteness(subtask, checkBox.isSelected());
                }
            } else {
                this.server.removeSubTask(this.cardObject.getCardList(), this.cardObject.getId(), subtask);
            }
        }
    }

    private void updateSubtaskCompleteness(final CardSubtask subtask, final boolean completed) {
        this.server.setSubtaskCompleteness(this.cardObject.getCardList(), this.cardObject.getId(), subtask, completed);
    }

    /**
     * Set the tags if they are not the same as the original.
     */
    public void setTags() {
        if (new HashSet<>(this.tags.getItems()).equals(this.cardObject.getTags())) {
            return;
        }
        Collection<Tag> unionOfTags = new HashSet<>(this.cardObject.getTags());
        unionOfTags.addAll(this.tags.getItems());
        for (final Tag tag : unionOfTags) {
            if (!this.cardObject.getTags().contains(tag)) {
                this.server.addTag(this.cardObject.getCardList(), this.cardObject.getId(), tag);
            }
            if (!this.tags.getItems().contains(tag)) {
                this.server.removeTag(this.cardObject.getCardList(), this.cardObject.getId(), tag);
            }
        }
    }

    /**
     * Set's the tagsList with card's information.
     *
     * @param card the card.
     */
    private void setTagList(final Card card) {
        this.tags.getItems().setAll(card.getTags());
    }

    /**
     * Set's the subtaskList with card's information.
     *
     * @param card the card.
     */
    private void setSubtaskList(final Card card) {
        this.subTasks.getItems().setAll(card.getSubtasks());
    }

    /**
     * Give listId a new value.
     *
     * @param theListId The variable it will be set to.
     */
    public void setListId(final long theListId) {
        this.listId = theListId;
    }

    /**
     * Set the fields of the cardDetails scene according to a card.
     *
     * @param card the Card instance to base the cardDetails scene on.
     */
    public void setFields(final Card card) {
        this.taskName.setText(card.getTitle());
        this.taskDescription.setText(card.getText());
        this.setTagList(card);
        this.setSubtaskList(card);
        this.cardObject = card;
        this.deleteButton.setVisible(true);
    }

    /**
     * Clear the fields of the cardDetails scene.
     */
    public void clearFields() {
        this.taskName.setText("");
        this.taskDescription.setText("");
        this.tags.getItems().clear();
        this.subTasks.getItems().clear();
        this.deleteButton.setVisible(false);
        this.cardObject = new Card();
    }

    /**
     * Delete a card.
     */
    public void deleteCard() {
        if (this.cardObject.getId() == 0) {
            return;
        }
        this.server.deleteCard(this.cardObject.getCardList(), this.cardObject.getId());
        this.showBoard();
    }

    /**
     * Handles the pressing of keys in this scene.
     *
     * @param keyEvent the event of pressing a key.
     */
    public void handleKeyPressed(final javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.H || "?".equals(keyEvent.getText()) || keyEvent.getCode() == KeyCode.SLASH) {
            //Show help screen.
            this.mainCtrl.showHelpAlert();
        }
        if (keyEvent.getCode() == KeyCode.ESCAPE) { //Exit the card details.
            this.showBoard();
        }
        if (keyEvent.getCode() == KeyCode.ENTER) { //Apply the new edits.
            this.apply();
        }
        if (keyEvent.getCode() == KeyCode.S) { //Add new subtask.
            this.addSubTasks();
        }
        if (keyEvent.getCode() == KeyCode.T) { //Open tag menu in card.
            this.addTag();
        }
        if (keyEvent.getCode() == KeyCode.D) { //Delete the card.
            this.deleteCard();
        }
    }
}
