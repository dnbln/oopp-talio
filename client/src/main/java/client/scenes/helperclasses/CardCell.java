package client.scenes.helperclasses;

import client.scenes.MainCtrl;
import commons.Card;
import commons.CardSubtask;
import commons.Tag;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.Set;


/**
 * The custom list cell that creates a cell from a card.
 */
public class CardCell extends ListCell<Card> {
    private static final double TAG_DISPLAY_WIDTH = 30.0;
    private static final double TAG_DISPLAY_HEIGHT = 5.0;
    private final MainCtrl mainCtrl;
    private long cardId;

    /**
     * Constructs a CardCell with a reference to the MainCtrl object.
     *
     * @param mainCtrl A reference to the MainCtrl object.
     */
    public CardCell(final MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    /**
     * The best method for developers to override to allow for them to customise the visuals of the cell.
     *
     * @param item  The card for the cell.
     * @param empty whether this cell represents data from the list. If it
     *              is empty, then it does not represent any domain data, but is a cell
     *              being used to render an "empty" row.
     */
    @Override
    protected void updateItem(final Card item, final boolean empty) {
        super.updateItem(item, empty);

        this.setBackground(Background.EMPTY);

        if (empty || item == null) {
            this.setGraphic(null);
            return;
        }

        this.cardId = item.getId();

        Button btn = new Button();
        btn.setAlignment(javafx.geometry.Pos.CENTER);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPadding(new Insets(10, 10, 10, 10));
        btn.setFocusTraversable(false);

        Label title = new Label(item.getTitle());
        title.setWrapText(true);
        VBox cardContent = new VBox(title);
        cardContent.setSpacing(5);
        cardContent.setAlignment(javafx.geometry.Pos.CENTER);

        if (!item.getText().isEmpty()) {
            Label descriptionIndicator = new Label("\u00B7\u00B7\u00B7");
            descriptionIndicator.setPadding(new Insets(-2, 0, -2, 0));
            cardContent.getChildren().add(descriptionIndicator);
        }

        // For the subtask show.
        int doneTasks = item.getSubtasks().stream().filter(CardSubtask::isCompleted).toArray().length;

        if (!item.getSubtasks().isEmpty()) {
            ProgressBar progressBar = new ProgressBar((double) doneTasks / item.getSubtasks().size());
            progressBar.setPrefHeight(10);
            progressBar.setMouseTransparent(true);
            cardContent.getChildren().add(progressBar);
        }

        FlowPane tagDisplay = new FlowPane();
        tagDisplay.setHgap(5);
        tagDisplay.setVgap(5);
        tagDisplay.setAlignment(javafx.geometry.Pos.CENTER);

        // Display the tags as colored rectangles in the flow pane as an HBox as rectangles.
        Set<Tag> tags = item.getTags();
        for (final Tag tag : tags) {
            Rectangle rectangle =
                    new Rectangle(TAG_DISPLAY_WIDTH, TAG_DISPLAY_HEIGHT, Color.web(tag.getBackgroundColor()));
            HBox hbox = new HBox(rectangle);
            hbox.setSpacing(10);
            tagDisplay.getChildren().add(hbox);
        }

        if (!item.getTags().isEmpty()) {
            cardContent.getChildren().add(tagDisplay);
        }

        btn.setGraphic(cardContent);

        final long maxWidth = 210L;
        btn.setMaxWidth(maxWidth);
        btn.setWrapText(true);

        btn.setOnAction(event -> this.mainCtrl.showCardDetails(item));

        VBox vBox = new VBox(btn);
        vBox.setAlignment(javafx.geometry.Pos.CENTER);

        // For the drag and drop
        btn.setOnDragDetected(e -> {
            this.getListView().getSelectionModel().select(this.getIndex());
            this.getListView().fireEvent(e);
        });

        // Set background color when cell is selected
        this.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected.booleanValue()) {
                this.setStyle("-fx-background-color: #cceeff;");
            } else {
                this.setStyle(null);
            }
        });

        this.setGraphic(vBox);
    }

    /**
     * Get cardId.
     *
     * @return cardId The value to get.
     */
    public long getCardId() {
        return this.cardId;
    }

    /**
     * Set the background color of the cell when it is selected.
     *
     * @param selected Whether the cell is selected.
     */
    @Override
    public void updateSelected(final boolean selected) {
        super.updateSelected(selected);

        if (selected) {
            this.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));
        } else {
            this.setBackground(Background.EMPTY);
        }
    }
}
