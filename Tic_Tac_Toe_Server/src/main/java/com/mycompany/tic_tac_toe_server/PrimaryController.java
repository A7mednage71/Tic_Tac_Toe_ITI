/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.tic_tac_toe_server;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author Hp
 */
public class PrimaryController implements Initializable {

    @FXML
    private Button startServer;
    @FXML
    private Button stopServer;
    @FXML
    private VBox playersList;
    @FXML
    private TextField searchPlayerTextField;

    private ServerThread serverThread;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        stopServer.setDisable(true);

        Platform.runLater(() -> {
            if (UserDAO.getInstance().getConnection() == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Database connection failed");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Database connected");
            }
        });
    }

    @FXML
    private void onStartServer(ActionEvent event) {
        if (UserDAO.getInstance().getConnection() == null) {
            showAlert(Alert.AlertType.ERROR, "Error", "Database not connected");
            return;
        }

        try {
            serverThread = new ServerThread();
            serverThread.start();

            startServer.setDisable(true);
            stopServer.setDisable(false);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Server started on port 5001");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to start server");
            e.printStackTrace();
        }
    }

    @FXML
    private void onStopServer(ActionEvent event) {
        if (serverThread != null) {
            serverThread.stopServer();
            serverThread = null;

            startServer.setDisable(false);
            stopServer.setDisable(true);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Server stopped");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

