package com.mycompany.finalprojectclient.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.mycompany.finalprojectclient.models.ResponseData;
import com.mycompany.finalprojectclient.models.ResponseStatus;
import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.utils.AuthManager;
import com.mycompany.finalprojectclient.utils.CustomAlertHandler;
import com.mycompany.finalprojectclient.utils.NavigationManager;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class RegisterController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Button registerButton;

    @FXML
    private StackPane customAlertOverlay;
    @FXML
    private VBox alertBox;
    @FXML
    private Label alertTitle;
    @FXML
    private Label alertMessage;
    @FXML
    private Label alertIcon;

    private CustomAlertHandler alertHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        alertHandler = new CustomAlertHandler(customAlertOverlay, alertBox, alertTitle, alertMessage, alertIcon);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!validateInput(username, password, confirmPassword)) {
            return;
        }

        setLoadingState(true);

        new Thread(() -> performRegistration(username, password, event)).start();
    }

    private boolean validateInput(String user, String pass, String confirm) {
        if (user.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            alertHandler.showError("MISSING INFO", "Please fill all fields.");
            return false;
        }
        if (user.length() < 3) {
            alertHandler.showError("INVALID USERNAME", "Username must be at least 3 characters.");
            return false;
        }
        if (pass.length() < 4) {
            alertHandler.showError("WEAK PASSWORD", "Password must be at least 4 characters.");
            return false;
        }
        if (!pass.equals(confirm)) {
            alertHandler.showError("PASSWORD MISMATCH", "Passwords do not match.");
            confirmPasswordField.clear();
            return false;
        }
        return true;
    }

    private void performRegistration(String username, String password, ActionEvent event) {
        ResponseData response = AuthManager.getInstance().register(username, password);

        Platform.runLater(() -> {
            setLoadingState(false);

            if (response.status == ResponseStatus.SUCCESS) {
                handleSuccess(response.message, event);
            } else {
                handleFailure(response.message);
            }
        });
    }

    private void handleSuccess(String message, ActionEvent event) {
        alertHandler.showSuccess("ACCOUNT CREATED!", message);

        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(e -> NavigationManager.switchScene(event, AppConstants.PATH_LOGIN));
        delay.play();
    }

    private void handleFailure(String message) {
        alertHandler.showError("REGISTRATION FAILED", message);
    }

    private void setLoadingState(boolean isLoading) {
        registerButton.setDisable(isLoading);
        if (isLoading) {
            registerButton.setText("Creating Account...");
        } else {
            registerButton.setText("Register");
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationManager.switchScene(event, AppConstants.PATH_LOGIN);
    }

    @FXML
    private void closeCustomAlert() {
        alertHandler.hide();
    }
}
