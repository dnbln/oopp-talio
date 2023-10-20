package client.scenes;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * The controller for logging in as an admin.
 */
public class AdminLoginCtrl {
    private final ServerConnectCtrl serverConnectCtrl;
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    /**
     * Constructor for the ServerConnectCtrl. It is annotated with @Inject which means that this constructor is used
     * when the injector is asked to get an instance of this class.
     *
     * @param serverConnectCtrl The connection to the main controller.
     */
    @Inject
    public AdminLoginCtrl(final ServerConnectCtrl serverConnectCtrl) {
        this.serverConnectCtrl = serverConnectCtrl;
    }

    /**
     * Connect to a server with given input.
     */
    @FXML
    public void loginButtonClicked() {
        String username = this.usernameField.getText();
        String password = this.passwordField.getText();
        if ("admin".equals(username) && "password".equals(password)) {
            // Login successful, close the dialog
            this.clearFields();
            this.serverConnectCtrl.connectAsAdmin();
        } else {
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText(null);
            alert.setContentText("Invalid username or password.");
            alert.showAndWait();
        }
    }

    /**
     * Clear the fields.
     */
    public void clearFields() {
        this.usernameField.setText("");
        this.passwordField.setText("");
    }
}
