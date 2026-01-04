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

public class AuthController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button loginButton;

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
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Error", "Fill all fields");
            return;
        }

        try {
            String response = ServerConnection.getInstance().sendRequest("LOGIN", new String[] { username, password });

            if (response.equals("LOGIN_SUCCESS")) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Welcome " + username);
                switchScene("Home.fxml", event);
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid credentials");
                passwordField.clear();
            }

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Error", "Server not available");
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        try {
            switchScene("register.fxml", event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}