package com.mycompany.finalprojectclient.controllers;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mycompany.finalprojectclient.models.GameSession;
import com.mycompany.finalprojectclient.models.RequestData;
import com.mycompany.finalprojectclient.models.RequestType;
import com.mycompany.finalprojectclient.models.ResponseData;
import com.mycompany.finalprojectclient.models.ResponseStatus;
import com.mycompany.finalprojectclient.network.ServerConnection;
import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.utils.AuthManager;
import com.mycompany.finalprojectclient.utils.CustomAlertHandler;
import com.mycompany.finalprojectclient.utils.NavigationManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

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
    @FXML
    private Label currentUserNameLabel;
    @FXML
    private Label currentUserScoreLabel;
    @FXML
    private Circle currentUserDot;

    private CustomAlertHandler alertHandler;
    private Map<String, UserRow> userRowMap = new HashMap<>();

    private static class UserRow {
        HBox row;
        Circle dot;
        Label scoreLabel;
        Label statusLabel;
        Button inviteButton;
        String username;
        String status;

        UserRow(HBox row, Circle dot, Label scoreLabel, Label statusLabel, Button inviteButton, String username) {
            this.row = row;
            this.dot = dot;
            this.scoreLabel = scoreLabel;
            this.statusLabel = statusLabel;
            this.inviteButton = inviteButton;
            this.username = username;
            this.status = "online";
        }

        void updateStatus(String newStatus) {
            this.status = newStatus;
            boolean isInGame = "in_game".equalsIgnoreCase(newStatus) || "busy".equalsIgnoreCase(newStatus);
            boolean isViewingHistory = "viewing_history".equalsIgnoreCase(newStatus);
            if (isInGame) {
                statusLabel.setText("In Game");
                statusLabel.setStyle("-fx-text-fill: #f39c12;");
                dot.setStyle("-fx-fill: #f39c12;");
                inviteButton.setText("Busy");
                inviteButton.setDisable(true);
                inviteButton.setStyle(
                        "-fx-background-color: rgba(74, 93, 35, 0.3); -fx-text-fill: #888; -fx-background-radius: 10; -fx-opacity: 0.6;");
            } else if (isViewingHistory) {
                statusLabel.setText("Game History");
                statusLabel.setStyle("-fx-text-fill: #3498db;");
                dot.setStyle("-fx-fill: #3498db;");
                inviteButton.setText("Busy");
                inviteButton.setDisable(true);
                inviteButton.setStyle(
                        "-fx-background-color: rgba(74, 93, 35, 0.3); -fx-text-fill: #888; -fx-background-radius: 10; -fx-opacity: 0.6;");
            } else {
                statusLabel.setText("Online");
                statusLabel.setStyle("-fx-text-fill: #888888;");
                dot.setStyle("-fx-fill: #50C878;");
                inviteButton.setText("Invite");
                inviteButton.setDisable(false);
                inviteButton.setStyle(
                        "-fx-background-color: #4A5D23; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
            }
        }

        void updateScore(int score) {
            if (scoreLabel != null) {
                scoreLabel.setText(String.valueOf(score));
            }
        }
    }

    @FXML
    private void handleBack() {
        try {
            closeConnection();
            NavigationManager.switchSceneUsingNode(backButton, AppConstants.PATH_ON_OFF);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleGameHistory() {
        try {
            GameSession.previousScreen = AppConstants.PATH_GAME_LOBBY;
            updatePlayerStatusToViewingHistory();
            NavigationManager.switchSceneUsingNode(backButton, AppConstants.PATH_GAME_HISTORY);
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

    private void updatePlayerStatusToViewingHistory() {
        try {
            String username = AuthManager.getInstance().getCurrentUsername();
            if (username != null && !username.isEmpty()) {
                RequestData request = new RequestData();
                request.key = RequestType.UPDATE_STATUS;
                request.username = username;
                request.status = "viewing_history";
                
                try {
                    ServerConnection.getInstance().sendRequest(request);
                    System.out.println("Status update sent to server: viewing_history");
                } catch (Exception e) {
                    System.err.println("Error updating status to viewing_history: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in updatePlayerStatusToViewingHistory: " + e.getMessage());
        }
    }

    private void updateCurrentUserStatusDisplay(String status) {
        boolean isInGame = "in_game".equalsIgnoreCase(status) || "busy".equalsIgnoreCase(status) || "viewing_history".equalsIgnoreCase(status);
        if (isInGame) {
            currentUserDot.setStyle("-fx-fill: #f39c12;");
        } else {
            currentUserDot.setStyle("-fx-fill: #50C878;");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        alertHandler = new CustomAlertHandler(alertOverlay, alertBox, alertTitle, alertMessage, alertIcon);

        ServerConnection.getInstance().setScoreListener((username, newScore) -> {
            Platform.runLater(() -> {
                String currentUser = AuthManager.getInstance().getCurrentUsername();
                if (currentUser != null && currentUser.equals(username)) {
                    currentUserScoreLabel.setText(String.valueOf(newScore));
                }

                UserRow userRow = userRowMap.get(username);
                if (userRow != null) {
                    userRow.updateScore(newScore);
                }
            });
        });

        ServerConnection.getInstance().setStatusListener((username, newStatus) -> {
            Platform.runLater(() -> {
                String currentUser = AuthManager.getInstance().getCurrentUsername();
                if (currentUser != null && currentUser.equals(username)) {
                    updateCurrentUserStatusDisplay(newStatus);
                }

                UserRow userRow = userRowMap.get(username);
                if (userRow != null) {
                    userRow.updateStatus(newStatus);
                }
            });
        });
        String currentUsername = AuthManager.getInstance().getCurrentUsername();
        if (currentUsername != null) {
            currentUserNameLabel.setText(currentUsername);
            new Thread(() -> {
                try {
                    RequestData request = new RequestData();
                    request.key = RequestType.GET_SCORE;
                    request.username = currentUsername;

                    ServerConnection.getInstance().sendRequest(request);
                } catch (Exception e) {
                    System.err.println("Error fetching current user score: " + e.getMessage());
                }
            }).start();
        }

        loadOnlineUsers();

        ServerConnection.getInstance().setNotificationListener(() -> {
            Platform.runLater(() -> loadOnlineUsers());
        });

        ServerConnection.getInstance().setInviteListener(new ServerConnection.InviteListener() {
            @Override
            public void onInviteReceived(String fromUsername) {
                Platform.runLater(() -> {
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
                            });
                });
            }

            @Override
            public void onInviteAccepted(String username) {
                Platform.runLater(() -> {
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
                Platform.runLater(() -> {
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
                Type mapType = new TypeToken<Map<String, String>>() {
                }.getType();
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
        boolean isViewingHistory = "viewing_history".equalsIgnoreCase(status);

        HBox userRow = new HBox(15);
        userRow.setPadding(new Insets(10));
        userRow.setStyle("-fx-border-color: transparent transparent #4A443F transparent;");
        userRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label name = new Label(username);
        name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

        Label scoreLabel = new Label("0");
        scoreLabel.setStyle("-fx-text-fill: #88a050; -fx-font-weight: bold;");

        Label statusLabel = new Label(isInGame ? "In Game" : (isViewingHistory ? "Game History" : "Online"));
        statusLabel.setStyle(isInGame ? "-fx-text-fill: #f39c12;" : (isViewingHistory ? "-fx-text-fill: #3498db;" : "-fx-text-fill: #888888;"));

        HBox statusRow = new HBox(8, scoreLabel, statusLabel);
        statusRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox info = new VBox(5, name, statusRow);
        HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);

        Circle dot = new Circle(4);
        dot.setStyle(isInGame ? "-fx-fill: #f39c12;" : (isViewingHistory ? "-fx-fill: #3498db;" : "-fx-fill: #50C878;"));

        Button invite = new Button(isInGame || isViewingHistory ? "Busy" : "Invite");
        if (isInGame || isViewingHistory) {
            invite.setDisable(true);
            invite.setStyle(
                    "-fx-background-color: rgba(74, 93, 35, 0.3); -fx-text-fill: #888; -fx-background-radius: 10; -fx-opacity: 0.6;");
        } else {
            invite.setStyle(
                    "-fx-background-color: #4A5D23; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
            invite.setOnAction(e -> sendInvite(username));
        }

        userRow.getChildren().addAll(info, dot, invite);
        usersContainer.getChildren().add(userRow);
        UserRow rowObj = new UserRow(userRow, dot, scoreLabel, statusLabel, invite, username);
        rowObj.status = status;
        userRowMap.put(username, rowObj);

        new Thread(() -> {
            try {
                RequestData request = new RequestData();
                request.key = RequestType.GET_SCORE;
                request.username = username;

                ServerConnection.getInstance().sendRequest(request);
            } catch (Exception e) {
                System.err.println("Error fetching score for " + username + ": " + e.getMessage());
            }
        }).start();
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

    private void disableAllInvites() {
        for (UserRow userRow : userRowMap.values()) {
            userRow.inviteButton.setDisable(true);
            userRow.inviteButton.setText("Disabled");
            userRow.inviteButton.setStyle(
                    "-fx-background-color: rgba(74, 93, 35, 0.3); -fx-text-fill: #888; -fx-background-radius: 10; -fx-opacity: 0.6;");
        }
    }

    private void enableAllInvites() {
        for (UserRow userRow : userRowMap.values()) {
            if (!userRow.status.equalsIgnoreCase("in_game") && 
                !userRow.status.equalsIgnoreCase("busy") && 
                !userRow.status.equalsIgnoreCase("viewing_history")) {
                userRow.inviteButton.setDisable(false);
                userRow.inviteButton.setText("Invite");
                userRow.inviteButton.setStyle(
                        "-fx-background-color: #4A5D23; -fx-text-fill: white; -fx-background-radius: 10; -fx-cursor: hand;");
                final String username = userRow.username;
                userRow.inviteButton.setOnAction(e -> sendInvite(username));
            }
        }
    }
}
