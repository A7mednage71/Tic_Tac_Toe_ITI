/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.finalprojectclient;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
/**
 * FXML Controller class
 *
 * @author DELL
 */
public class BoardController implements Initializable {
    private void switchScene(String fxml, ActionEvent e) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/finalprojectclient/" + fxml));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }


    @FXML
    private GridPane gameGrid;
    @FXML
    private Label scoreX;
    @FXML
    private Label scoreO;
    /**
     * Initializes the controller class.
     */
 private String playerX;
private String playerO;
@Override
public void initialize(URL location, ResourceBundle resources) {

    if (GameSession.vsComputer) {

        playerX = "You";
        playerO = "Computer (" + GameSession.difficulty + ")";

        System.out.println("Playing vs Computer");
        System.out.println("Difficulty: " + GameSession.difficulty);

        switch (GameSession.difficulty) {
            case EASY:
                //initEasyAI();
                break;
            case MEDIUM:
                //initMediumAI();
                break;
            case HARD:
               // initHardAI();
                break;
        }

    } else {
        playerX = "Player 1";
        playerO = "Player 2";
        System.out.println("Two Players mode");
    }

    updateScoreLabels();
}



    @FXML
    private void handleCellClick(ActionEvent event) {
    }

    @FXML
    private void handlePlayAgain(ActionEvent event) {
    }

    @FXML
    private void handleBack(ActionEvent event) {
         try {
            switchScene("vsComputer.fxml", event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
    private void updateScoreLabels() {
    scoreX.setText(playerX + " (X): 0");
    scoreO.setText(playerO + " (O): 0");
}



    
}
