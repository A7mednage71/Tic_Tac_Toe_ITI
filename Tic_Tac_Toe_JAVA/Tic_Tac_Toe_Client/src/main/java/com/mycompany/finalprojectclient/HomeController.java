package com.mycompany.finalprojectclient;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HomeController{
    private void switchScene(String fxml, ActionEvent e) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/finalprojectclient/" + fxml));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @FXML
    private void playVsComputer(ActionEvent event) {
         try {
            switchScene("vsComputer.fxml", event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
      
    }

    @FXML
    private void twoPlayers(ActionEvent event) {
         try {
            switchScene("TicTacToeLobby.fxml", event);
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

    
}
