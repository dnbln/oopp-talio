package client.scenes;

import client.scenes.helperclasses.TagCell;
import client.utils.ServerUtilsInterface;
import com.google.inject.Inject;
import commons.Tag;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

/**
 * The add tag controller.
 */
public class AddTagCtrl {
    private final ServerUtilsInterface server;
    private ObservableList<? super Tag> tags;
    private Stage popupStage;

    private MainCtrl mainCtrl;

    @FXML
    private ListView<Tag> tagList;

    @FXML
    private Button addTagsToCardButton;

    @FXML
    private TextField tagName;

    @FXML
    private ColorPicker tagColor;

    @FXML
    private ColorPicker fontColor;

    /**
     * Constructor for the CardDetailsCtrl. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param server   The connection to the server.
     * @param mainCtrl The connection to the main controller.
     */
    @Inject
    public AddTagCtrl(final ServerUtilsInterface server, final MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;

    }

    @SuppressWarnings("MagicNumber")
    private String getHexText(final Color color) {
// https://stackoverflow.com/questions/3942878/how-to-decide-font-color-in-white-or-black-depending-on-background-color
        final int maxRGBValue = 256;
        double red = maxRGBValue * color.getRed();
        double green = maxRGBValue * color.getGreen();
        double blue = maxRGBValue * color.getBlue();

        // RGB to sRGB
        List<Double> uiColors = List.of(red / (maxRGBValue - 1), green / (maxRGBValue - 1), blue / (maxRGBValue - 1));
        List<Double> cols = uiColors.stream().map(col -> {
            if (col.doubleValue() <= 0.03928) {
                return col.doubleValue() / 12.92;
            }
            return StrictMath.pow((col.doubleValue() + 0.055) / 1.055, 2.4);
        }).toList();

        double luminance = (0.2126 * cols.get(0).doubleValue()) + (0.7152 * cols.get(1).doubleValue()) +
                           (0.0722 * cols.get(2).doubleValue());
        return (luminance > 0.179) ? "#000000" : "#ffffff";
    }

    /**
     * Initialize the AddTagCtrl class.
     *
     * @param theTags          The tag list to which the tag will be added.
     * @param thePopupStage    The stage of the popup.
     * @param showAddTagButton boolean whether to show the add tags to card button.
     */
    public void initialize(final ObservableList<? super Tag> theTags, final Stage thePopupStage,
                           final boolean showAddTagButton) {
        this.tags = theTags;
        this.popupStage = thePopupStage;
        this.tagList.setCellFactory(param -> new TagCell(this.server::deleteTag));

        this.addTagsToCardButton.setVisible(showAddTagButton);
        this.addTagsToCardButton.setManaged(showAddTagButton);

        // When the tag background color is changed, the font color is changed accordingly.
        this.tagColor.setOnAction(event -> {
            Color color = this.tagColor.getValue();
            String hexText = this.getHexText(color);
            this.fontColor.setValue(Color.web(hexText));
        });

        this.tagList.getItems().setAll(this.server.getAllTags());
        this.tagList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

        // When a tag is selected, its values are shown in the fields.
        this.tagList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.tagName.setText(newValue.getName());
                this.tagColor.setValue(Color.web(newValue.getBackgroundColor()));
                this.fontColor.setValue(Color.web(newValue.getFontColor()));
            }
        });

        this.tagName.setText("New tag");
    }

    /**
     * Add the selected tag to the tag list and close the popup.
     */
    @FXML
    public void addTagsToCard() {
        this.tags.addAll(this.tagList.getSelectionModel().getSelectedItems());
        this.popupStage.close();
    }

    /**
     * Add a new tag to the board.
     */
    @FXML
    public void addTagToBoard() {
        final String newTagName = this.tagName.getText().trim();
        if (newTagName.isEmpty()) {
            return;
        }

        Tag tag = new Tag();
        Color color = this.tagColor.getValue();
        Color textColor = this.fontColor.getValue();
        String colorString = color.toString().replace("0x", "#");
        String fontColorString = textColor.toString().replace("0x", "#");
        tag.patch(tagPatcher -> {
            tagPatcher.setName(newTagName);
            tagPatcher.setBackgroundColor(colorString);
            tagPatcher.setFontColor(fontColorString);
        });

        Tag newTag = this.server.newTag(tag);
        this.tagList.getItems().add(newTag);
    }


    /**
     * Change the background color of the selected tag and close the popup.
     */
    @FXML
    public void changeTagColors() {
        if (this.tagList.getSelectionModel().getSelectedItems().isEmpty()) {
            final Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setTitle("Tag is not selected!");
            alert.setHeaderText("Please select a tag first!");
            alert.showAndWait();
            return;
        }
        Color color = this.tagColor.getValue();
        Color textColor = this.fontColor.getValue();
        String backColorString = color.toString().replace("0x", "#");
        String fontColorString = textColor.toString().replace("0x", "#");
        Tag selected = this.tagList.getSelectionModel().getSelectedItems().get(0);
        if (!selected.getBackgroundColor().equals(backColorString)) {
            this.server.changeTagBackgroundColor(selected.getId(), backColorString);
        }
        if (!selected.getFontColor().equals(fontColorString)) {
            this.server.changeTagFontColor(selected.getId(), fontColorString);
        }
        this.initialize(this.tags, this.popupStage, true);
    }


    /**
     * Handles the pressing of keys in this scene.
     *
     * @param keyEvent the event of pressing a key.
     */
    public void handleKeyPressed(final javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.H || "?".equals(keyEvent.getText()) || keyEvent.getCode() == KeyCode.SLASH) {
            mainCtrl.showHelpAlert();
        }
        if (keyEvent.getCode() == KeyCode.ESCAPE) { //Close tag menu.
            this.popupStage.close();
        }
    }
}
