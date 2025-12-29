package com.mycompany.finalprojectclient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class HomeController{

    @FXML
    private void playVsComputer(ActionEvent event) {
        System.out.println("Play vs Computer clicked");
        // هنا تفتح شاشة اللعب ضد الكمبيوتر
    }

    @FXML
    private void twoPlayers(ActionEvent event) {
        System.out.println("Two Players clicked");
        // هنا تفتح شاشة Two Players
    }

    
}
