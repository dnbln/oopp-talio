package client.scenes.helperclasses;

import commons.Tag;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.TextAlignment;

import java.util.function.Consumer;

/**
 * The custom list cell for the tags.
 */
public class TagCell extends ListCell<Tag> {
    private final Label label = new Label();
    private final Button button = new Button("x");
    private final HBox hbox = new HBox(this.label, this.button);
    private final Consumer<? super Tag> runBeforeRemoveTag;

    /**
     * Constructor for the TagCell.
     */
    public TagCell() {
        this(tag -> {
        });
    }

    /**
     * Constructor for the TagCell.
     *
     * @param runBeforeRemoveTag The method to call when the button is pressed.
     */
    public TagCell(final Consumer<? super Tag> runBeforeRemoveTag) {
        this.runBeforeRemoveTag = runBeforeRemoveTag;
    }

    /**
     * The best method for developers to override to allow for them to customise the visuals of the cell.
     *
     * @param item  The new item for the cell.
     * @param empty whether this cell represents data from the list. If it
     *              is empty, then it does not represent any domain data, but is a cell
     *              being used to render an "empty" row.
     */
    @Override
    protected void updateItem(final Tag item, final boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            this.setGraphic(null);
            return;
        }

        this.label.setText(item.getName());

        final int backgroundRadius = 3;
        final int labelPaddingTop = 1;
        final int labelPaddingRight = 5;
        final int labelPaddingBottom = 1;
        final int labelPaddingLeft = 5;
        final int buttonFontSize = 8;
        final int hBoxSpacing = 5;

        this.label.setStyle("-fx-background-color: %s; -fx-background-radius: %d; -fx-text-fill: %s;".formatted(
                item.getBackgroundColor(), backgroundRadius, item.getFontColor()));

        this.label.setPadding(new Insets(labelPaddingTop, labelPaddingRight, labelPaddingBottom, labelPaddingLeft));

        this.button.setTextAlignment(TextAlignment.CENTER);
        this.button.setStyle("-fx-font-size: %d;".formatted(buttonFontSize));
        this.button.setOnAction(event -> {
            this.runBeforeRemoveTag.accept(item);
            this.getListView().getItems().remove(item);
        });

        this.hbox.setSpacing(hBoxSpacing);
        this.hbox.setAlignment(Pos.TOP_CENTER);
        this.hbox.setFillHeight(false);

        this.setAlignment(Pos.TOP_CENTER);

        // add event handlers for checkbox and button here
        this.setGraphic(this.hbox);
    }
}
