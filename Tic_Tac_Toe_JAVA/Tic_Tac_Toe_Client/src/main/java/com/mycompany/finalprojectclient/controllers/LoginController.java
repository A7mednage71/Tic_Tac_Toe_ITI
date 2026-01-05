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

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

   
    @FXML
    private StackPane customAlertOverlay;
    @FXML
    private VBox alertBox;
    @FXML
    private Label alertTitle;
    @FXML
    private Label alertMessage;

    private CustomAlertHandler alertHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        alertHandler = new CustomAlertHandler(customAlertOverlay, alertBox, alertTitle, alertMessage);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            alertHandler.showError("MISSING INFO", "Please enter your username and password.");
            return;
        }

        setLoadingState(true);

        new Thread(() -> performLogin(username, password, event)).start();
    }

    private void performLogin(String username, String password, ActionEvent event) {
    ResponseData response = AuthManager.getInstance().login(username, password);

    Platform.runLater(() -> {
        setLoadingState(false);
        if (response.status == ResponseStatus.SUCCESS) {
            alertHandler.showSuccess("WELCOME HERO!", "Hello " + username.toUpperCase() + ", redirecting...");

            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> NavigationManager.switchScene(event, AppConstants.PATH_GAME_LOBBY));
            delay.play();

        } else {
           
            String errorMsg = response.message != null ? response.message : "Invalid username or password.";
            
            if (errorMsg.toUpperCase().contains("ALREADY")) {
                alertHandler.showError("DUPLICATE LOGIN", "This account is already logged in from another device.");
            } else {
                alertHandler.showError("ACCESS DENIED", errorMsg);
            }
            
            passwordField.clear();
        }
    });
}

    @FXML
    private void handleSignUp(ActionEvent event) {
        NavigationManager.switchScene(event, AppConstants.PATH_REGISTER);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        NavigationManager.switchScene(event, AppConstants.PATH_ON_OFF);
    }

    @FXML
    private void closeCustomAlert() {
        alertHandler.hide();
    }

    private void setLoadingState(boolean isLoading) {
        loginButton.setDisable(isLoading);
        loginButton.setText(isLoading ? "Connecting..." : "Login");
    }
}