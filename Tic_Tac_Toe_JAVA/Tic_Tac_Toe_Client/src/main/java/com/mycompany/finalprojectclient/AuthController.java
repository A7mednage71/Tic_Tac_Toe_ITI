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

    
    private String send(String action, Object... data) throws Exception {
        return "test";
    }

    
    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            switchScene("Home.fxml", event);
        } catch (Exception ex) {
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