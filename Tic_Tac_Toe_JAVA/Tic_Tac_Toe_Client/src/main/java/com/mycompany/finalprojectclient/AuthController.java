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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AuthController {

    @FXML private TextField txtUser;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPass;

    private void switchScene(String fxml, ActionEvent e) throws Exception {
        Parent root = FXMLLoader.load(
            getClass().getResource("/com/mycompany/finalprojectclient/" + fxml)
        );
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private String send(String action, Object... data) throws Exception {
        try (Socket socket = new Socket("localhost", 5000)) {
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            dos.writeUTF(action);
            for (Object d : data) {
                dos.writeUTF(d.toString());
            }

            return dis.readUTF();
        }
    }

    @FXML
    public void login(ActionEvent e) {
        try {
            String res = send("LOGIN", txtEmail.getText(), txtPass.getText());
            if (res.equals("LOGIN_SUCCESS")) {
                switchScene("game.fxml", e);
            } else {
                System.out.println("Login failed: " + res);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void register(ActionEvent e) {
        try {
            String res = send("REGISTER", txtUser.getText(), txtEmail.getText(), txtPass.getText());
            if (res.equals("REGISTER_SUCCESS")) {
                switchScene("login.fxml", e);
            } else {
                System.out.println("Register failed: " + res);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    public void goRegister(ActionEvent e) throws Exception {
        switchScene("register.fxml", e);
    }

    @FXML
    public void goLogin(ActionEvent e) throws Exception {
        switchScene("login.fxml", e);
    }
}
