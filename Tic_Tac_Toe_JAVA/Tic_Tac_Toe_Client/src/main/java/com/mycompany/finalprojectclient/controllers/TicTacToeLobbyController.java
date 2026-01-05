package com.mycompany.finalprojectclient.controllers;

import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.utils.NavigationManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;

public class TicTacToeLobbyController implements Initializable {

    @FXML
    private VBox usersContainer;
    @FXML
    private Button backButton;

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            NavigationManager.switchScene(event, AppConstants.PATH_HOME);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        addUser("ashraf", true);
        addUser("abdelhamid", false);
        addUser("nageh", false);
        addUser("Shady", true);
        addUser("ibrahim", true);
        addUser("nada", true);
    }

    private void addUser(String username, boolean online) {
        HBox userRow = new HBox(15);
        userRow.setPadding(new Insets(10));
        userRow.setStyle("-fx-border-color: transparent transparent #4A443F transparent;");
        userRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label name = new Label(username);
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label status = new Label(online ? "Online" : "Offline");
        status.setStyle("-fx-text-fill: #888888;");

        VBox info = new VBox(5, name, status);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        Circle dot = new Circle(4);
        dot.setStyle(online ? "-fx-fill: #50C878;" : "-fx-fill: gray;");

        Button invite = new Button("Invite");
        invite.setStyle("-fx-background-color: #4A5D23; -fx-text-fill: white; -fx-background-radius: 5;");

        userRow.getChildren().addAll(info, dot, invite);
        usersContainer.getChildren().add(userRow);
    }
}
