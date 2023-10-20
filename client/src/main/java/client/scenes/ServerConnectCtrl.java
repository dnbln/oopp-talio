package client.scenes;

import client.utils.InvalidServerException;
import client.utils.ServerUtilsInterface;
import client.utils.WebsocketConnectionException;
import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URISyntaxException;

/**
 * The controller for the server connect scene.
 */
public class ServerConnectCtrl {
    private final ServerUtilsInterface server;
    private final MainCtrl mainCtrl;

    private static final Logger LOGGER = LogManager.getLogger();

    @FXML
    private Label errorLabel;

    @FXML
    private TextField serverInput;

    /**
     * Constructor for the ServerConnectCtrl. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param server   The connection to the server.
     * @param mainCtrl The connection to the main controller.
     */
    @Inject
    public ServerConnectCtrl(final ServerUtilsInterface server, final MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
        this.server = server;

    }

    /**
     * Connect to a server with given input.
     */
    public void connect() {
        if (this.isValid(this.serverInput.getText())) {
            this.errorLabel.setText("");
            MainCtrl.setAsAdmin(false);
            this.mainCtrl.showBoardList();
        }
    }

    /**
     * Validate a server address and returns the address.
     *
     * @param address The unvalidated server address.
     * @return The validated server address.
     */
    boolean isValid(final String address) {
        try {
            this.server.validateAndSetServer(address);
        } catch (final InvalidServerException e) {
            this.showErrorMessage(e, "The provided URL is not of a valid Talio server. Try another server.");
            return false;
        } catch (final URISyntaxException e) {
            this.showErrorMessage(e, "The provided input was not a valid URL. Check your spelling and try again");
            return false;
        } catch (final InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            this.showErrorMessage(e,
                    "Something unexpected happened. If you are experiencing trouble, reload the application");
            return false;
        } catch (final WebsocketConnectionException | RuntimeException e) {
            this.showErrorMessage(e,
                    "Could not connect to the Talio server. Check the spelling or your connection and try again.");
            return false;
        }
        return true;
    }

    /**
     * Show an error message.
     *
     * @param e            The exception that caused the error.
     * @param errorMessage The error message to show.
     */
    void showErrorMessage(final Exception e, final String errorMessage) {
        this.errorLabel.setText(errorMessage);
        LOGGER.error(e.getMessage());
    }

    /**
     * Connect to a server and verify if the user is indeed and admin.
     */
    public void verifyAdmin() {
        if (this.isValid(this.serverInput.getText())) {
            this.errorLabel.setText("");
            this.mainCtrl.showAdminLogin();
        }
    }

    /**
     * Connect to the server as an admin.
     */
    public void connectAsAdmin() {
        MainCtrl.setAsAdmin(true);
        this.mainCtrl.showBoardList();
    }

    /**
     * Propagates the command to shut down the long polling thread.
     */
    public void stopLongPollingThread() {
        this.server.stopLongPollingThread();
    }

    /**
     * Handles the pressing of keys in this scene.
     *
     * @param keyEvent the event of pressing a key.
     */
    public void handleKeyPressed(final javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.H || "?".equals(keyEvent.getText()) || keyEvent.getCode() == KeyCode.SLASH) {
            //Show help screen.
            mainCtrl.showHelpAlert();
        }
    }
}
