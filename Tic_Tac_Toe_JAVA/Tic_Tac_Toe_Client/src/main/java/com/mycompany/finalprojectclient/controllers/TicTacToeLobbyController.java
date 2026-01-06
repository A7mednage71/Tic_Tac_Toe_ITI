package com.mycompany.finalprojectclient.controllers;

import com.mycompany.finalprojectclient.network.ServerConnection;
import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.utils.NavigationManager;
import com.mycompany.finalprojectclient.utils.AuthManager;
import com.mycompany.finalprojectclient.models.RequestData;
import com.mycompany.finalprojectclient.models.RequestType;
import com.mycompany.finalprojectclient.models.ResponseData;
import com.mycompany.finalprojectclient.models.ResponseStatus;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class TicTacToeLobbyController implements Initializable {

    @FXML
    private VBox usersContainer;
    @FXML
    private Button backButton;

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            closeConnection();
            NavigationManager.switchScene(event, AppConstants.PATH_HOME);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            ServerConnection.getInstance().disconnect();
            System.out.println("Connection closed from login screen");
        } catch (Exception e) {
            System.err.println("Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadOnlineUsers();

        ServerConnection.getInstance().setNotificationListener(() -> {
            javafx.application.Platform.runLater(() -> loadOnlineUsers());
        });
    }

    private void loadOnlineUsers() {
        try {
            RequestData request = new RequestData();
            request.key = RequestType.GET_ONLINE_USERS;

            String response = ServerConnection.getInstance().sendRequest(request);
            ResponseData responseData = new Gson().fromJson(response, ResponseData.class);
            if (responseData.status == ResponseStatus.SUCCESS) {
                Type listType = new TypeToken<List<String>>() {
                }.getType();
                List<String> onlineUsers = new Gson().fromJson(responseData.message, listType);
                usersContainer.getChildren().clear();
                String currentUser = AuthManager.getInstance().getCurrentUsername();
                for (String username : onlineUsers) {
                    if (currentUser != null && username.equalsIgnoreCase(currentUser)) {
                        continue;
                    }
                    addUser(username, true);
                }
                if (onlineUsers.isEmpty() || (onlineUsers.size() == 1 && currentUser != null)) {
                    Label noUsersLabel = new Label("No online users available");
                    noUsersLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14;");
                    usersContainer.getChildren().add(noUsersLabel);
                }
            }
        } catch (Exception e) {
        }
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
