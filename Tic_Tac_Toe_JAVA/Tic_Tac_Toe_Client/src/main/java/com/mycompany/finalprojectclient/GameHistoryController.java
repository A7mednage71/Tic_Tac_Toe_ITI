/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.finalprojectclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

/**
 *
 * @author Hp
 */
public class GameHistoryController {
    private void switchScene(String fxml, ActionEvent e) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/finalprojectclient/" + fxml));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private ImageView iconImage;
    @FXML
    private TableView<?> historyTable;
    @FXML
    private Button btnBack;
    @FXML
    private Button btnReplay;
    @FXML
    private TableColumn<?, ?> colGameId;
    @FXML
    private TableColumn<?, ?> colPlayer1;
    @FXML
    private TableColumn<?, ?> colMove1;
    @FXML
    private TableColumn<?, ?> colPlayer2;
    @FXML
    private TableColumn<?, ?> colMove2;
    @FXML
    private TableColumn<?, ?> colWinner;
    @FXML
    private TableColumn<?, ?> colDateTime;

    @FXML
    private void handleBack(ActionEvent event) {
        
        try {
            switchScene("online&offline.fxml", event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleReplay(ActionEvent event) {
    }
    
}
