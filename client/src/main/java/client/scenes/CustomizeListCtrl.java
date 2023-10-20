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
public class CustomizeListCtrl {
    private final ServerUtilsInterface server;
    private final MainCtrl mainCtrl;
    private Stage popupStage;

    @FXML
    private ColorPicker cardListColor;

    @FXML
    private ColorPicker cardListFontColor;

    private CardListViewCtrl ctrl;

    private long listId;

    /**
     * Constructor for the CardDetailsCtrl. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param server   The connection to the server.
     * @param mainCtrl The connection to the main controller.
     */
    @Inject
    public CustomizeListCtrl(final ServerUtilsInterface server, final MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;

    }

    /**
     * Initialize the AddTagCtrl class.
     *
     * @param thePopupStage The stage of the popup.
     * @param listId The if of the list.
     * @param ctrl  the controller of the list view.
     */
    public void initialize(final Stage thePopupStage, final long listId, final CardListViewCtrl ctrl) {
        this.popupStage = thePopupStage;
        this.listId = listId;
        this.ctrl = ctrl;
    }

    /**
     * Apply the color changes and close the popup.
     */
    @FXML
    public void apply() {
        Color color = this.cardListColor.getValue();
        String cardListBackground = color.toString().replace("0x", "#");

        Color color2 = this.cardListFontColor.getValue();
        String cardListFont = color2.toString().replace("0x", "#");

        this.server.setCardListBackgroundColor(this.listId, cardListBackground);
        this.server.setCardListFontColor(this.listId, cardListFont);

        this.popupStage.close();
        this.ctrl.applyCustomize(this.listId);
    }

    /**
     * Reset the color changes and close the popup.
     */
    @FXML
    public void reset() {
        this.server.setCardListBackgroundColor(this.listId, "#ffffff");
        this.server.setCardListFontColor(this.listId, "#000000");

        this.popupStage.close();
        this.ctrl.resetCustomize();
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

