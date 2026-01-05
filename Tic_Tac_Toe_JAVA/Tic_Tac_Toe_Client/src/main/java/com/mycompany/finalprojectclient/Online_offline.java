package com.mycompany.finalprojectclient;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

public class Online_offline implements Initializable {

    private void switchScene(String fxml, ActionEvent e) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/finalprojectclient/" + fxml));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
    @FXML
    private Button onlineButton;

    @FXML
    private Button offlineButton;

    @FXML
    private Button historyButton;
    @FXML
    private Button backButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        System.out.println("Home Screen Loaded!");
    }

    @FXML
    private void handleOnlinePlay(ActionEvent event) {
        try {
            switchScene("login.fxml", event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }

    @FXML
    private void handleOfflinePlay(ActionEvent event) {
        GameSession.vsComputer = false; 
           try {
            switchScene("Board.fxml", event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }

    @FXML
    private void gameHistory(ActionEvent event) {
        try {
            switchScene("game_history.fxml", event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }

    
   

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            switchScene("Home.fxml", event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }
}
