package client.scenes;

import client.utils.ServerUtilsInterface;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import commons.Card;
import commons.Tag;
import commons.events.BoardRemovedEvent;
import commons.events.CardRemovedEvent;
import commons.events.ServerToClientEvent;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Collection;

/**
 * The main controller. From here all other scenes are displayed.
 */
public class MainCtrl {
    private static boolean asAdmin = false;
    private Stage primaryStage;
    private final Collection<Stage> popUps = new ArrayList<>();

    private BoardOverviewCtrl overviewCtrl;
    private Scene overview;

    private Scene serverConnect;

    private AdminLoginCtrl adminLoginCtrl;
    private Scene adminLogin;

    private CardDetailsCtrl cardDetailsCtrl;
    private Scene cardDetails;

    private BoardListCtrl boardListCtrl;
    private Scene boardList;

    private AddTagCtrl addTagCtrl;
    private Scene addTag;

    private CustomizeCtrl customizeCtrl;
    private Scene customize;

    private CustomizeListCtrl customizeListCtrl;
    private Scene customizeList;

    private Injector cardListViewInjector;

    private static final double MIN_HEIGHT = 400.0;
    private static final double MIN_WIDTH = 600.0;
    private static final double MIN_HEIGHT_CONNECT = 200.0;
    private static final double MIN_WIDTH_CONNECT = 350.0;

    private final ServerUtilsInterface server;

    /**
     * Constructor for the MainCtrl. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param server The connection to the server.
     */
    @Inject
    public MainCtrl(final ServerUtilsInterface server) {
        this.server = server;
    }

    /**
     * Initialize the MainCtrl class.
     *
     * @param thePrimaryStage    The stage which will be taken as primary.
     * @param theOverview        A Pair of the overview window and the parent.
     * @param theServerConnect   A Pair of the server connect window and the parent.
     * @param theAdminLogin      A Pair of the admin login window and the parent.
     * @param theCardDetails     A pair pf the card details window and the parent.
     * @param theBoardList       A pair of the board list and its parent.
     * @param theAddTag          A pair of the add tag window and its parent.
     * @param theCustomize       A pair of the customize window and its parent.
     * @param theCustomizeList   A pair of the customizeList window and its parent.
     * @param cardListViewInject This injector from which CardListView instances can be gotten.
     */
    public void initialize(final Stage thePrimaryStage,
                           final Pair<? extends BoardOverviewCtrl, ? extends Parent> theOverview,
                           final Pair<? extends ServerConnectCtrl, ? extends Parent> theServerConnect,
                           final Pair<? extends AdminLoginCtrl, ? extends Parent> theAdminLogin,
                           final Pair<? extends CardDetailsCtrl, ? extends Parent> theCardDetails,
                           final Pair<? extends BoardListCtrl, ? extends Parent> theBoardList,
                           final Pair<? extends AddTagCtrl, ? extends Parent> theAddTag,
                           final Pair<? extends CustomizeCtrl, ? extends Parent> theCustomize,
                           final Pair<? extends CustomizeListCtrl, ? extends Parent> theCustomizeList,
                           final Injector cardListViewInject) {

        this.primaryStage = thePrimaryStage;
        this.overviewCtrl = theOverview.getKey();
        this.overview = new Scene(theOverview.getValue());

        this.serverConnect = new Scene(theServerConnect.getValue());

        this.adminLoginCtrl = theAdminLogin.getKey();
        this.adminLogin = new Scene(theAdminLogin.getValue());

        this.cardDetailsCtrl = theCardDetails.getKey();
        this.cardDetails = new Scene(theCardDetails.getValue());

        this.boardListCtrl = theBoardList.getKey();
        this.boardList = new Scene(theBoardList.getValue());

        this.addTagCtrl = theAddTag.getKey();
        this.addTag = new Scene(theAddTag.getValue());

        this.customizeCtrl = theCustomize.getKey();
        this.customize = new Scene(theCustomize.getValue());

        this.customizeListCtrl = theCustomizeList.getKey();
        this.customizeList = new Scene(theCustomizeList.getValue());

        this.cardListViewInjector = cardListViewInject;

        this.showConnect();
        this.primaryStage.setMinHeight(MIN_HEIGHT);
        this.primaryStage.setMinWidth(MIN_WIDTH);
    }

    /**
     * Get asAdmin.
     *
     * @return asAdmin The value to get.
     */
    public static boolean isAsAdmin() {
        return asAdmin;
    }

    /**
     * Give asAdmin a new value.
     *
     * @param asAdmin The variable it will be set to.
     */
    public static void setAsAdmin(final boolean asAdmin) {
        MainCtrl.asAdmin = asAdmin;
    }

    /**
     * Get CardListViewPair.
     *
     * @return CardListViewPair The value to get.
     */
    // I think you can't mock Injectors so testing this is going to be a pain.
    @SuppressWarnings("unchecked")
    public Pair<CardListViewCtrl, Parent> getCardListViewPair() {
        return this.cardListViewInjector.getInstance(Key.get(Pair.class, Names.named("CardListViewPair")));
    }

    /**
     * Show the overview window.
     */
    void showOverview() {
        this.showOverviewWithBoardId("");
    }

    /**
     * Show the overview window, include the board id in the title.
     */
    void showOverviewWithBoardId(final String boardIdString) {
        this.popUps.forEach(Stage::close);
        this.popUps.clear();
        if (!this.primaryStage.isShowing()) {
            this.primaryStage.show();
        }

        this.primaryStage.setTitle("Talio - Board %s".formatted(boardIdString));
        this.primaryStage.setScene(this.overview);
        this.overviewCtrl.reloadBoard(null);

        this.server.stopLongPollingThread();
    }

    /**
     * Show the addTag popup.
     */
    void showAddTag(final ObservableList<? super Tag> subtasks, final boolean showAddTagToCard) {
        final Stage popUpAddTag = new Stage();
        popUpAddTag.initModality(Modality.APPLICATION_MODAL);
        popUpAddTag.initOwner(this.primaryStage);
        popUpAddTag.setTitle("Add Tag");

        this.addTagCtrl.initialize(subtasks, popUpAddTag, showAddTagToCard);

        popUpAddTag.setScene(this.addTag);
        popUpAddTag.show();

        this.popUps.add(popUpAddTag);
    }

    /**
     * Shows the customization scene for the board.
     *
     * @param boardOverviewCtrl the boardOverview controller.
     */
    void showCustomize(final BoardOverviewCtrl boardOverviewCtrl) {
        final Stage popUpCustomize = new Stage();
        popUpCustomize.initModality(Modality.APPLICATION_MODAL);
        popUpCustomize.initOwner(this.primaryStage);
        popUpCustomize.setTitle("Customize Your Board");

        this.customizeCtrl.initialize(popUpCustomize, boardOverviewCtrl);

        popUpCustomize.setScene(this.customize);
        popUpCustomize.show();

        this.popUps.add(popUpCustomize);
    }

    /**
     * Shows the customization scene for the list.
     */
    void showListCustomize(final long listId, final CardListViewCtrl ctrl) {
        final Stage popUpCustomizeList = new Stage();
        popUpCustomizeList.initModality(Modality.APPLICATION_MODAL);
        popUpCustomizeList.initOwner(this.primaryStage);
        popUpCustomizeList.setTitle("Customize Your List");

        this.customizeListCtrl.initialize(popUpCustomizeList, listId, ctrl);

        popUpCustomizeList.setScene(this.customizeList);
        popUpCustomizeList.show();

        this.popUps.add(popUpCustomizeList);
    }

    /**
     * Show the connection window.
     */
    void showConnect() {
        if (this.primaryStage.isShowing()) {
            this.primaryStage.hide();
        }
        Stage connectStage = new Stage();
        connectStage.resizableProperty().setValue(false);
        connectStage.setMinHeight(MIN_HEIGHT_CONNECT);
        connectStage.setMinWidth(MIN_WIDTH_CONNECT);
        connectStage.setTitle("Connect to server");
        connectStage.setScene(this.serverConnect);
        connectStage.show();
        this.popUps.add(connectStage);
    }

    /**
     * Show the card details window.
     */
    void showCardDetails(final long listId) {
        this.showCardDetails(listId, null);
    }

    /**
     * Displays the details of a Card object.
     *
     * @param card the Card object to display details for.
     */
    public void showCardDetails(final Card card) {
        this.showCardDetails(0, card);
    }

    @SuppressWarnings("checkstyle:DesignForExtension")
    void showCardDetails(final long listId, final Card card) {
        final Stage popUpCardDetails = new Stage();
        popUpCardDetails.initModality(Modality.APPLICATION_MODAL);
        popUpCardDetails.initOwner(this.primaryStage);

        popUpCardDetails.setTitle("Card Details: Card Overview");
        if (listId != 0) {
            this.cardDetailsCtrl.setListId(listId);
        }
        if (card != null) {
            this.cardDetailsCtrl.setFields(card);
        }

        popUpCardDetails.setOnCloseRequest(event -> this.cardDetailsCtrl.clearFields());

        popUpCardDetails.setScene(this.cardDetails);
        popUpCardDetails.show();
        this.popUps.add(popUpCardDetails);
    }

    /**
     * Show the board list window.
     */
    public void showBoardList() {
        this.popUps.forEach(Stage::close);
        if (!this.primaryStage.isShowing()) {
            this.primaryStage.show();
        }

        this.primaryStage.setTitle("Board List");
        this.primaryStage.setScene(this.boardList);

        this.server.registerForUpdates(event -> Platform.runLater(this::showBoardList));
        this.boardListCtrl.initData();
    }

    /**
     * Handles a server-to-client event by logging it and reloading the overview board in the JavaFX application thread.
     *
     * @param event the server-to-client event to handle
     */
    public void handleUpdate(final ServerToClientEvent event) {
        Platform.runLater(() -> {
            if (event instanceof BoardRemovedEvent) {
                showBoardRemovedAlert();
                this.showBoardList();
                return;
            }
            if (event instanceof CardRemovedEvent e) {
                if (this.cardDetails.getWindow().isShowing()) {
                    this.cardDetailsCtrl.cardDeleted(e);
                }
            }
            this.overviewCtrl.reloadBoard(event);
        });
    }

    private static void showBoardRemovedAlert() {
        final Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setContentText("It seems the board you are on has been deleted.");
        alert.setHeaderText("Return to the board list");
        alert.showAndWait();
    }

    /**
     * Shows an alert with information about the keyboard shortcuts.
     */
    public void showHelpAlert() {

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Keyboard Shortcuts");
        alert.setHeaderText("List of Available Keyboard Shortcuts:");
        alert.setContentText("""
                Keyboard Shortcuts:
                Press "?", "Slash" or "H" anywhere in the application to open a help screen of shortcuts
                (for some devices it may be "Shift+?").
                The whole application is iterable through the tab button. You can know the selected item by looking at
                which one has the blue background/border.
                In Board List, press:
                    "C" to create new board,
                    "D" to delete the selected board,
                    "R" to remove the selected board from workshop,
                    "E" to enter the invite link board,
                    "N" to change the name of the selected board,
                    "Enter" (or shift + enter for some devices) to connect to the selected board.
                In Board Overview, press:
                    "W" to go to the workspace,
                    "D" to disconnect,
                    "A" to add a new list.
                    "T" for a popup for adding tags,
                    "C" for a popup for color preset selection.
                    Hover with the mouse over a task selects it.
                    Selected tasks can have shortcuts activated on them.
                    Move the task highlight with arrow keys (Up/Down/Left/Right), so you can select other tasks.
                    Press "Shift+Up/Down or U/D" to change the order of tasks in the list.
                    Press "Shift+Left/Right or L/R" to move the tasks to the list to its left or right.
                    Press "E" to edit the selected task title directly in the overview.
                    Press "Del" or "Backspace" to delete a task, so you do not have to open the details.
                    Press "Enter" or "N" to open the task details, (for some devices it may be "Shift+Enter").
                In Card Details:
                    Press "Esc" to close the task details.
                    Press "Enter" to apply the new info.
                    Press "S" to add new subtask.
                    Press "T" to open tag menu.
                    Press "D" to delete the card.
                In Tag Menu:
                    Press "Esc" to close the menu.
                In Customization Menu:
                    Press "Esc" to close the menu.
                """);
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setPrefWidth(MIN_WIDTH);
        alert.showAndWait();
    }

    /**
     * Show the admin login window.
     */
    public void showAdminLogin() {
        final Stage popUpAdminLogin = new Stage();
        popUpAdminLogin.initModality(Modality.APPLICATION_MODAL);
        popUpAdminLogin.initOwner(this.primaryStage);
        popUpAdminLogin.setTitle("Admin Login");

        popUpAdminLogin.setOnCloseRequest(event -> this.adminLoginCtrl.clearFields());
        popUpAdminLogin.setScene(this.adminLogin);
        popUpAdminLogin.show();

        this.popUps.add(popUpAdminLogin);
    }
}
