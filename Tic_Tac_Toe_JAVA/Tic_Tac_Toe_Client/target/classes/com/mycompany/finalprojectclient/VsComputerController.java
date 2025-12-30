package com.mycompany.finalprojectclient;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;

public class VsComputerController {

    @FXML
    private void playEasy(ActionEvent event) {
        System.out.println("Easy mode selected");
        // هنا هتضيف منطق اللعبة للـ Easy
    }

    @FXML
    private void playMedium(ActionEvent event) {
        System.out.println("Medium mode selected");
        // هنا هتضيف منطق اللعبة للـ Medium
    }

    @FXML
    private void playHard(ActionEvent event) {
        System.out.println("Hard mode selected");
        // هنا هتضيف منطق اللعبة للـ Hard
    }
}
