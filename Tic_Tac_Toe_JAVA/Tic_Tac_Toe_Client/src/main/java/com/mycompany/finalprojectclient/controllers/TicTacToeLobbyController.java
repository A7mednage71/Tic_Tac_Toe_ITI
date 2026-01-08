package com.mycompany.finalprojectclient.controllers;

import com.mycompany.finalprojectclient.network.ServerConnection;
import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.utils.NavigationManager;
import com.mycompany.finalprojectclient.utils.AuthManager;
import com.mycompany.finalprojectclient.utils.CustomAlertHandler;
import com.mycompany.finalprojectclient.models.RequestData;
import com.mycompany.finalprojectclient.models.RequestType;
import com.mycompany.finalprojectclient.models.ResponseData;
import com.mycompany.finalprojectclient.models.ResponseStatus;
import com.mycompany.finalprojectclient.models.GameSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private StackPane alertOverlay;
    @FXML
    private VBox alertBox;
    @FXML
    private Label alertTitle;
    @FXML
    private Label alertMessage;
    @FXML
    private Label alertIcon;

    private CustomAlertHandler alertHandler;
    private Map<String, UserRow> userRowMap = new HashMap<>();

    private static class UserRow {
        HBox row;
        Circle dot;
        Label statusLabel;
        Button inviteButton;
        String username;
        String status; 

        UserRow(HBox row, Circle dot, Label statusLabel, Button inviteButton, String username) {
            this.row = row;
            this.dot = dot;
            this.statusLabel = statusLabel;
            this.inviteButton = inviteButton;
            this.username = username;
            this.status = "online";
        }

        void updateStatus(String newStatus) {
            this.status = newStatus;
            boolean isInGame = "in_game".equalsIgnoreCase(newStatus) || "busy".equalsIgnoreCase(newStatus);
            if (isInGame) {
               statusLabel.setText("In Game");
                statusLabel.setStyle("-fx-text-fill: #f39c12;");
                dot.setStyle("-fx-fill: #f39c12;");
                inviteButton.setText("Busy");
                inviteButton.setDisable(true);
                inviteButton.setStyle("-fx-background-color: rgba(74, 93, 35, 0.3); -fx-text-fill: #888; -fx-background-radius: 10; -fx-opacity: 0.6;");
            } else {
                statusLabel.setText("Online");
                statusLabel.setStyle("-fx-text-fill: #888888;");
                dot.setStyle("-fx-fill: #50C878;");
                inviteButton.setText("Invite");
                inviteButton.setDisable(false);
                inviteButton.setStyle("-fx-background-color: #4A5D23; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
            }
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            closeConnection();
            NavigationManager.switchScene(event, AppConstants.PATH_ON_OFF);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleGameHistory(ActionEvent event) {
        try {
            NavigationManager.switchScene(event, AppConstants.PATH_GAME_HISTORY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void closeConnection() {
        try {
            ServerConnection.getInstance().disconnect();
            System.out.println("Connection closed from lobby");
        } catch (Exception e) {
            System.err.println("Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        alertHandler = new CustomAlertHandler(alertOverlay, alertBox, alertTitle, alertMessage, alertIcon);
        
        loadOnlineUsers();

        ServerConnection.getInstance().setNotificationListener(() -> {
            javafx.application.Platform.runLater(() -> loadOnlineUsers());
        });

        ServerConnection.getInstance().setInviteListener(new ServerConnection.InviteListener() {
            @Override
            public void onInviteReceived(String fromUsername) {
                javafx.application.Platform.runLater(() -> {
                    alertHandler.showConfirmation(
                        "Game Invitation",
                        fromUsername + " wants to play with you!",
                        new CustomAlertHandler.ConfirmationCallback() {
                            @Override
                            public void onYes() {
                                acceptInvite(fromUsername);
                            }

                            @Override
                            public void onNo() {
                                rejectInvite(fromUsername);
                            }
                        }
                    );
                });
            }

            @Override
            public void onInviteAccepted(String username) {
                javafx.application.Platform.runLater(() -> {
                    alertHandler.hide(); 
                    
                    
                    updateUserStatus(username, "in_game");
                    updateUserStatus(AuthManager.getInstance().getCurrentUsername(), "in_game");
                    
                    
                    GameSession.isOnline = true;
                    GameSession.vsComputer = false;
                    GameSession.opponentName = username;
                    GameSession.playerSymbol = "X"; 
                    
                    
                    
                    
                    NavigationManager.switchSceneUsingNode(usersContainer, AppConstants.PATH_GAME_BOARD);
                });
            }

            @Override
            public void onInviteRejected(String username) {
                javafx.application.Platform.runLater(() -> {
                    alertHandler.hide(); 
                    alertHandler.showError("Invitation Rejected", username + " rejected your invitation.");
                });
            }

            @Override
            public void onOpponentWithdrew(String username) {
            }

            @Override
            public void onPlayAgainRequested(String username) {
            }

            @Override
            public void onGameStart(String symbol, String opponent) {
            }
        });
    }
    private void loadOnlineUsers() {
        try {
            RequestData request = new RequestData();
            request.key = RequestType.GET_ONLINE_USERS;

            String response = ServerConnection.getInstance().sendRequest(request);
            ResponseData responseData = new Gson().fromJson(response, ResponseData.class);
            if (responseData.status == ResponseStatus.SUCCESS) {
                Type mapType = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> onlineUsersMap = new Gson().fromJson(responseData.message, mapType);
                
                usersContainer.getChildren().clear();
                userRowMap.clear();
                
                String currentUser = AuthManager.getInstance().getCurrentUsername();
                for (Map.Entry<String, String> entry : onlineUsersMap.entrySet()) {
                    String username = entry.getKey();
                    String status = entry.getValue();
                    
                    if (currentUser != null && username.equalsIgnoreCase(currentUser)) {
                        continue;
                    }
                    addUser(username, status);
                }
                if (onlineUsersMap.isEmpty() || (onlineUsersMap.size() == 1 && currentUser != null)) {
                    Label noUsersLabel = new Label("No online users available");
                    noUsersLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 14;");
                    usersContainer.getChildren().add(noUsersLabel);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addUser(String username, String status) {
        boolean isInGame = "in_game".equalsIgnoreCase(status) || "busy".equalsIgnoreCase(status);
       
        HBox userRow = new HBox(15);
        userRow.setPadding(new Insets(10));
        userRow.setStyle("-fx-border-color: transparent transparent #4A443F transparent;");
        userRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label name = new Label(username);
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label statusLabel = new Label(isInGame ? "In Game" : "Online");
        statusLabel.setStyle(isInGame ? "-fx-text-fill: #f39c12;" : "-fx-text-fill: #888888;");

        VBox info = new VBox(5, name, statusLabel);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        Circle dot = new Circle(4);
        dot.setStyle(isInGame ? "-fx-fill: #f39c12;" : "-fx-fill: #50C878;");

        Button invite = new Button(isInGame ? "Busy" : "Invite");
        if (isInGame) {
            invite.setDisable(true);
            invite.setStyle("-fx-background-color: rgba(74, 93, 35, 0.3); -fx-text-fill: #888; -fx-background-radius: 10; -fx-opacity: 0.6;");
        } else {
            invite.setStyle("-fx-background-color: #4A5D23; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
            invite.setOnAction(e -> sendInvite(username));
        }
 
        userRow.getChildren().addAll(info, dot, invite);
        usersContainer.getChildren().add(userRow);

        // Store reference
        UserRow rowObj = new UserRow(userRow, dot, statusLabel, invite, username);
        rowObj.status = status;
        userRowMap.put(username, rowObj);
    }

    private void sendInvite(String targetUsername) {
        try {
            RequestData request = new RequestData();
            request.key = RequestType.SEND_INVITE;
            request.username = AuthManager.getInstance().getCurrentUsername();
            request.targetUsername = targetUsername;

            ServerConnection.getInstance().sendRequest(request);
            alertHandler.showLoading("Waiting", "Waiting for " + targetUsername + " to respond...");
            System.out.println("Invite sent to: " + targetUsername);
        } catch (Exception e) {
            e.printStackTrace();
            alertHandler.showError("Error", "Failed to send invitation");
        }
    }

    private void acceptInvite(String fromUsername) {
        try {
            RequestData request = new RequestData();
            request.key = RequestType.ACCEPT_INVITE;
            request.username = AuthManager.getInstance().getCurrentUsername();
            request.targetUsername = fromUsername;

            ServerConnection.getInstance().sendRequest(request);
            
            GameSession.isOnline = true;
            GameSession.vsComputer = false;
            GameSession.opponentName = fromUsername;
            GameSession.playerSymbol = "O"; 
            
            updateUserStatus(fromUsername, "in_game");
            updateUserStatus(AuthManager.getInstance().getCurrentUsername(), "in_game");
            
            System.out.println("Invite accepted from: " + fromUsername);
            
            NavigationManager.switchSceneUsingNode(usersContainer, AppConstants.PATH_GAME_BOARD);
            
        } catch (Exception e) {
            e.printStackTrace();
            alertHandler.showError("Error", "Failed to accept invitation");
        }
    }

    private void rejectInvite(String fromUsername) {
        try {
            RequestData request = new RequestData();
            request.key = RequestType.REJECT_INVITE;
            request.username = AuthManager.getInstance().getCurrentUsername();
            request.targetUsername = fromUsername;

            ServerConnection.getInstance().sendRequest(request);
            System.out.println("Invite rejected from: " + fromUsername);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUserStatus(String username, String status) {
        UserRow userRow = userRowMap.get(username);
        if (userRow != null) {
            userRow.updateStatus(status);
        }
    }
}

