package com.mycompany.finalprojectclient.controllers;

import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.utils.NavigationManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class HomeController {

    @FXML
    private void playVsComputer(ActionEvent event) {
        try {
            NavigationManager.switchScene(event, AppConstants.PATH_VS_PC);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @FXML
    private void twoPlayers(ActionEvent event) {
        try {
            NavigationManager.switchScene(event, AppConstants.PATH_ON_OFF);
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

}
