package com.mycompany.finalprojectclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

    private String send(String action, Object... data) {
       return "TEST";
    }

    @FXML
private void handleRegister(ActionEvent event) {
    String username = usernameField.getText();
    String password = passwordField.getText();
    String confirmPassword = confirmPasswordField.getText();

    if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
        System.out.println("Please fill all fields");
        return;
    }

    if (!password.equals(confirmPassword)) {
        System.out.println("Passwords do not match!");
        return;
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