/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.finalprojectclient;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
/**
 * FXML Controller class
 *
 * @author DELL
 */
public class BoardController implements Initializable {


    @FXML
    private GridPane gameGrid;
    @FXML
    private Label scoreX;
    @FXML
    private Label scoreO;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void handleCellClick(ActionEvent event) {
    }

    @FXML
    private void handlePlayAgain(ActionEvent event) {
    }

    @FXML
    private void handleBack(ActionEvent event) {
    }


    
}
