package com.mycompany.finalprojectclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

    private void switchScene(String fxml, ActionEvent e) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/finalprojectclient/" + fxml));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Error", "Fill all fields");
            return;
        }

        if (username.length() < 3) {
            showAlert(Alert.AlertType.WARNING, "Error", "Username too short");
            return;
        }

        if (password.length() < 4) {
            showAlert(Alert.AlertType.WARNING, "Error", "Password too short");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.WARNING, "Error", "Passwords don't match");
            confirmPasswordField.clear();
            return;
        }

        try {
            String response = ServerConnection.getInstance().sendRequest("REGISTER",
                    new String[] { username, password });

            if (response.equals("REGISTER_SUCCESS")) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Account created");
                switchScene("login.fxml", event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Username taken");
                usernameField.clear();
                passwordField.clear();
                confirmPasswordField.clear();
            }

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Server not available");
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            switchScene("login.fxml", event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}