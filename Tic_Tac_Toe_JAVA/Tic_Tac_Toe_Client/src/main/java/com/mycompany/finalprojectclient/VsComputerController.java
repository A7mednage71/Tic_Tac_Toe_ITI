package com.mycompany.finalprojectclient;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class VsComputerController {
    private void switchScene(String fxml, ActionEvent e) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/finalprojectclient/" + fxml));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private Button backButton;

    @FXML
    private void playEasy(ActionEvent event) {
        System.out.println("Easy mode selected");
       
    }

    @FXML
    private void playMedium(ActionEvent event) {
        System.out.println("Medium mode selected");
         
    }

    @FXML
    private void playHard(ActionEvent event) {
        System.out.println("Hard mode selected");
        
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
