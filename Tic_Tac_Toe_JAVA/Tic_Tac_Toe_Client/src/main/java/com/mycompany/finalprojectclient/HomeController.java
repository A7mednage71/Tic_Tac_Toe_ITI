package com.mycompany.finalprojectclient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class HomeController{

    @FXML
    private void playVsComputer(ActionEvent event) {
        System.out.println("Play vs Computer clicked");
    }

    @FXML
    private void twoPlayers(ActionEvent event) {
        System.out.println("Two Players clicked");
    }

    
}
