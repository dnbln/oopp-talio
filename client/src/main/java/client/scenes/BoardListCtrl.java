package client.scenes;

import client.utils.ServerUtilsInterface;
import client.utils.Workspace;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import commons.Board;
import jakarta.ws.rs.NotFoundException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.Optional;

/**
 * Controls the board list view in the application.
 */
public class BoardListCtrl {
    private final Workspace workspace;
    private final ServerUtilsInterface server;
    private final MainCtrl mainCtrl;

    @FXML
    private ListView<Board> boardListView;

    @FXML
    private Button addBoardButton;

    @FXML
    private Button removeBoardButton;

    /**
     * Constructor for the ServerConnectCtrl. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param server    The connection to the server.
     * @param mainCtrl  The connection to the main controller.
     * @param workspace The workspace of the user.
     */
    @Inject
    public BoardListCtrl(final ServerUtilsInterface server, final MainCtrl mainCtrl, final Workspace workspace) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.workspace = workspace;
    }

    /**
     * Creates a new board. Asks for the title of the new board.
     * Alerts the user if no title was given.
     */
    public void createNewBoard() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create new board");
        dialog.setHeaderText("Please enter the title of the new board that is about to be created");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(title -> {
            if (title.trim().isEmpty()) {
                showTitleAlert();
                this.createNewBoard();
            } else {
                final long newBoardId = this.server.addBoard().getId();
                this.server.setBoardTitle(newBoardId, title);
                if (!MainCtrl.isAsAdmin()) {
                    this.workspace.addBoardId(newBoardId);
                }
                this.initData();
            }
        });
    }

    private static void showTitleAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid input");
        alert.setHeaderText("Board title is required");
        alert.setContentText("Please enter a valid title.");
        alert.showAndWait();
    }

    /**
     * deletes a board.
     */
    @FXML
    private void deleteBoard() {
        Board selectedBoard = this.boardListView.getSelectionModel().getSelectedItem();
        if (selectedBoard == null) {
            return;
        }

        this.server.deleteBoard(selectedBoard.getId());
    }

    /**
     * Changes the title of the selected board.
     */
    @FXML
    private void changeBoardTitle() {
        Board selectedBoard = this.boardListView.getSelectionModel().getSelectedItem();
        if (selectedBoard == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog(selectedBoard.getTitle());
        dialog.setTitle("Board Settings");
        dialog.setHeaderText("Please enter the new title of this board");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(title -> {
            if (title.trim().isEmpty()) {
                showTitleAlert();
                this.changeBoardTitle();
            } else {
                this.server.setBoardTitle(selectedBoard.getId(), title);
            }
        });
    }

    /**
     * subscribes to the selected board.
     *
     * @throws JsonProcessingException an exception
     */
    public void goToSelectedBoard() throws JsonProcessingException {
        Board selectedBoard = this.boardListView.getSelectionModel().getSelectedItem();
        if (selectedBoard == null) {
            return;
        }

        long id = selectedBoard.getId();
        this.server.subscribeToBoard(id);
        this.mainCtrl.showOverviewWithBoardId(Long.toString(id));
    }

    /**
     * Initialises the list of boards in the scene.
     */
    public void initData() {
        this.addBoardButton.setVisible(!MainCtrl.isAsAdmin());
        this.addBoardButton.setManaged(!MainCtrl.isAsAdmin());
        this.removeBoardButton.setVisible(!MainCtrl.isAsAdmin());
        this.removeBoardButton.setManaged(!MainCtrl.isAsAdmin());

        this.boardListView.setCellFactory(listView -> new BoardListCell());
        List<Board> list;
        if (MainCtrl.isAsAdmin()) {
            list = this.server.getBoards();
        } else {
            List<Long> ids = this.workspace.getBoardIds();
            list = this.server.getBoards(ids);
            this.workspace.cleanupBoardIds(list.stream().map(Board::getId).toList());
        }
        this.boardListView.setItems(FXCollections.observableArrayList(list));
        if (!list.isEmpty()) {
            this.boardListView.getSelectionModel().selectLast();
        }

        this.boardListView.addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            try {
                this.handleKeyPressed(keyEvent);
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * A popup will appear in which you can enter the ID of the board you want to join.
     */
    public void addNewBoard() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Join board");
        dialog.setHeaderText("Please enter the ID of the board you want to join");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(id -> {
            try {
                long boardId = Long.parseLong(id);
                Board board = this.server.getBoard(boardId);
                this.workspace.addBoardId(boardId);
                this.initData();
            } catch (final NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Invalid input");
                alert.setHeaderText("Board ID is required");
                alert.setContentText("Please enter a valid ID.");
                alert.showAndWait();
                this.addNewBoard();
            } catch (final NotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Board Not Found");
                alert.setHeaderText("The board does not exist in the database");
                alert.setContentText("Please enter a valid board ID.");
                alert.showAndWait();
                this.addNewBoard();
            }
        });
    }

    /**
     * Removes the selected board from the workspace.
     */
    public void removeBoard() {
        Board selectedBoard = this.boardListView.getSelectionModel().getSelectedItem();
        if (selectedBoard == null) {
            return;
        }

        this.workspace.removeBoardId(selectedBoard.getId());
        this.initData();
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
                this.mainCtrl.showConnect();
            }
        });
    }

    /**
     * Handles the pressing of keys in this scene.
     *
     * @param keyEvent the event of pressing a key.
     * @throws JsonProcessingException an exception
     */
    public void handleKeyPressed(final javafx.scene.input.KeyEvent keyEvent) throws JsonProcessingException {
        if (keyEvent.getText().equals("?") || keyEvent.getCode() == KeyCode.H || keyEvent.getCode() == KeyCode.SLASH) {
            //Show help screen.
            this.mainCtrl.showHelpAlert();
        }
        if (keyEvent.getCode() == KeyCode.C) { //Create new board.
            this.createNewBoard();
        }
        if (keyEvent.getCode() == KeyCode.D) { //Delete selected board.
            this.deleteBoard();
        }
        if (keyEvent.getCode() == KeyCode.N) { //Change board title.
            this.changeBoardTitle();
        }
        if (keyEvent.getCode() == KeyCode.R) { //Remove board from workspace.
            this.removeBoard();
        }
        if (keyEvent.getCode() == KeyCode.E) { //Enter the invite link to add new board.
            this.addNewBoard();
        }
        if (keyEvent.getCode() == KeyCode.ENTER) { //Join the selected board.
            this.goToSelectedBoard();
        }
    }

    private static class BoardListCell extends ListCell<Board> {
        @Override
        protected void updateItem(final Board item, final boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                this.setText(null);
            } else {
                this.setText("%s (Board with id: %d)".formatted(item.getTitle(), item.getId()));
            }
        }
    }
}

