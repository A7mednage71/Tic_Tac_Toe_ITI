package com.mycompany.finalprojectclient.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import com.mycompany.finalprojectclient.models.GameSession;
import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.utils.NavigationManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

public class Online_offline implements Initializable {

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
            if (com.mycompany.finalprojectclient.network.ServerConnection.getInstance().isConnected() && 
                com.mycompany.finalprojectclient.utils.AuthManager.getInstance().getCurrentUsername() != null) {
                NavigationManager.switchScene(event, AppConstants.PATH_GAME_LOBBY);
            } else {
                NavigationManager.switchScene(event, AppConstants.PATH_LOGIN);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleOfflinePlay(ActionEvent event) {
        GameSession.vsComputer = false;
        try {
            NavigationManager.switchScene(event, AppConstants.PATH_GAME_BOARD);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @FXML
    private void gameHistory(ActionEvent event) {
        try {
            NavigationManager.switchScene(event, AppConstants.PATH_GAME_HISTORY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            if (com.mycompany.finalprojectclient.network.ServerConnection.getInstance().isConnected()) {
                com.mycompany.finalprojectclient.network.ServerConnection.getInstance().disconnect();
            }
            NavigationManager.switchScene(event, AppConstants.PATH_HOME);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
