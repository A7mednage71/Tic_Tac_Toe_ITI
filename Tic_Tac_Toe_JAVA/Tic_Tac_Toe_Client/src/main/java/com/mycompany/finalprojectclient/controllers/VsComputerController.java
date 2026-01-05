package com.mycompany.finalprojectclient.controllers;

import com.mycompany.finalprojectclient.models.GameSession;
import com.mycompany.finalprojectclient.models.GameSession.Difficulty;
import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.utils.NavigationManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class VsComputerController {

    @FXML
    private void playEasy(ActionEvent event) {
        try {
            GameSession.vsComputer = true;
            GameSession.difficulty = Difficulty.EASY;
            NavigationManager.switchScene(event, AppConstants.PATH_GAME_BOARD);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void playMedium(ActionEvent event) {
        try {
            GameSession.vsComputer = true;
            GameSession.difficulty = Difficulty.MEDIUM;
            NavigationManager.switchScene(event, AppConstants.PATH_GAME_BOARD);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void playHard(ActionEvent event) {
        try {
            GameSession.vsComputer = true;
            GameSession.difficulty = Difficulty.HARD;
            NavigationManager.switchScene(event, AppConstants.PATH_GAME_BOARD);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            NavigationManager.switchScene(event, AppConstants.PATH_HOME);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
