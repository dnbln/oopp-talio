package client.scenes.helperclasses;

import commons.CardSubtask;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.text.TextAlignment;

/**
 * The custom list cell for the subtasks.
 */
public class SubTaskCell extends ListCell<CardSubtask> {
    private final CheckBox checkBox = new CheckBox();
    private final Button button = new Button("X");
    private final TilePane tilePane = new TilePane(this.button);
    private final HBox hbox = new HBox(this.checkBox, this.tilePane);
    private long subtaskId;

    /**
     * The best method for developers to override to allow for them to customise the visuals of the cell.
     *
     * @param item  The new item for the cell.
     * @param empty whether this cell represents data from the list. If it
     *              is empty, then it does not represent any domain data, but is a cell
     *              being used to render an "empty" row.
     */
    @Override
    protected void updateItem(final CardSubtask item, final boolean empty) {
        super.updateItem(item, empty);

        this.setStyle("-fx-background-color: transparent;");

        // bind the width of each cell to the preferred width of the list
        this.prefWidthProperty().bind(this.getListView().widthProperty().subtract(5));
        // set the max width of each cell to its preferred size
        this.setMaxWidth(Region.USE_PREF_SIZE);

        if (empty) {
            this.setGraphic(null);
            return;
        }

        this.subtaskId = item.getId();

        this.checkBox.setText(item.getName());
        this.checkBox.setSelected(item.isCompleted());

        // Set the CSS selector of the checkbox equal to the ID of the subtask for later referencing.
        this.checkBox.setId(Long.toString(item.getId()));

        Tooltip.install(this.checkBox, new Tooltip(item.getName()));

        this.button.setTextAlignment(TextAlignment.CENTER);
        this.button.setOnAction(event -> this.getListView().getItems().remove(item));

        final int buttonFontSize = 8;
        final int tilePaneRightPadding = 8;
        this.button.setStyle("-fx-font-size: %d".formatted(buttonFontSize));

        // To evade the scrollbar.
        this.tilePane.setPadding(new Insets(0, tilePaneRightPadding, 0, 0));
        this.tilePane.setAlignment(Pos.CENTER_RIGHT);
        this.tilePane.setPrefWidth(this.tilePane.getChildren().get(0).prefWidth(-1));

        HBox.setHgrow(this.tilePane, Priority.ALWAYS);
        // add event handlers for checkbox and button here
        this.setGraphic(this.hbox);
    }

    /**
     * Get the subtask ID.
     * @return subtaskId
     */
    public long getSubtaskId() {
        return this.subtaskId;
    }
}
