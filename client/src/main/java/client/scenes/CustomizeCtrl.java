package client.scenes;

import client.utils.ServerUtilsInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.ColorPicker;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * The add tag controller.
 */
public class CustomizeCtrl {
    private final ServerUtilsInterface server;
    private final MainCtrl mainCtrl;
    private Stage popupStage;

    private BoardOverviewCtrl boardOverviewCtrl;

    @FXML
    private ColorPicker boardColor;

    @FXML
    private ColorPicker boardFontColor;

    /**
     * Constructor for the CardDetailsCtrl. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param server   The connection to the server.
     * @param mainCtrl The connection to the main controller.
     */
    @Inject
    public CustomizeCtrl(final ServerUtilsInterface server, final MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;

    }

    /**
     * Initialize the AddTagCtrl class.
     *
     * @param thePopupStage The stage of the popup.
     * @param boardOverviewCtrl The controller of the board overview.
     */
    public void initialize(final Stage thePopupStage, final BoardOverviewCtrl boardOverviewCtrl) {
        this.popupStage = thePopupStage;
        this.boardOverviewCtrl = boardOverviewCtrl;
    }

    /**
     * Add the color changes and close the popup.
     */
    @FXML
    public void apply() {
        Color color = this.boardColor.getValue();
        String boardBackground = color.toString().replace("0x", "#");

        Color color2 = this.boardFontColor.getValue();
        String boardFont = color2.toString().replace("0x", "#");

        this.server.setBoardBackgroundColor(boardBackground);
        this.server.setBoardFontColor(boardFont);

        this.popupStage.close();
        this.boardOverviewCtrl.applyCustomize();
    }

    /**
     * Reset the color changes and close the popup.
     */
    @FXML
    public void reset() {
        this.server.setBoardBackgroundColor("");
        this.server.setBoardFontColor("");

        this.popupStage.close();
        this.boardOverviewCtrl.resetCustomize();
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
            this.mainCtrl.showHelpAlert();
        }
        if (keyEvent.getCode() == KeyCode.ESCAPE) { //Close the customization menu.
            this.popupStage.close();
        }
        if (keyEvent.getCode() == KeyCode.A) { //Apply
            this.apply();
        }
    }
}

