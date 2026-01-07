package com.mycompany.tic_tac_toe_server.controllers;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import com.mycompany.tic_tac_toe_server.utils.CustomAlert;
import com.mycompany.tic_tac_toe_server.database.UserDAO;
import com.mycompany.tic_tac_toe_server.models.PlayerModel;
import com.mycompany.tic_tac_toe_server.network.ServerThread;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Window;

public class PrimaryController implements Initializable {

    private Boolean started = false;

    @FXML
    private Button startServer;
    @FXML
    private ListView<PlayerModel> playersList;
    @FXML
    private TextField searchPlayerTextField;

    @FXML
    private Label activeCountLabel;
    @FXML
    private Label inGameCountLabel;
    @FXML
    private Label offlineCountLabel;

    private ServerThread serverThread;

    private ObservableList<PlayerModel> playersObservableList;
    private boolean keepUpdating = true;

    private Label emptyTitleLabel;
    private Label emptyHintLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        startServer.setText("🚀 Start Server");
        startServer.setStyle("-fx-background-color: #2ed573;");

        checkDatabaseConnection();
        setupPlaceholder();
        setupPlayersList();
        startListUpdater();
    }

    private Window getWindow() {
        if (startServer.getScene() != null) {
            return startServer.getScene().getWindow();
        }
        return null;
    }

    private void checkDatabaseConnection() {
        Platform.runLater(() -> {
            if (UserDAO.getInstance().getConnection() == null) {
                CustomAlert.show(null, Alert.AlertType.ERROR, "Error", "Database connection failed");
            }
        });
    }

    @FXML
    private void onStartServer(ActionEvent event) {
        if (started) {
            stopServer();
        } else {
            startServer();
        }
    }

    private void startServer() {
        if (UserDAO.getInstance().getConnection() == null) {
            CustomAlert.show(getWindow(), Alert.AlertType.ERROR, "Error", "Database not connected");
            return;
        }
        try {
            serverThread = new ServerThread();
            serverThread.start();

            CustomAlert.show(getWindow(), Alert.AlertType.INFORMATION, "Success", "Server started");

            startServer.setText("⏹ Stop Server");
            startServer.setStyle("-fx-background-color: #ff4757;");
            started = true;

        } catch (Exception e) {
            CustomAlert.show(getWindow(), Alert.AlertType.ERROR, "Error", "Failed to start server");
            e.printStackTrace();
        }
    }

    private void stopServer() {
        if (serverThread != null) {
            serverThread.stopServer();
            serverThread = null;

            startServer.setText("🚀 Start Server");
            startServer.setStyle("-fx-background-color: #2ed573;");
            started = false;

            CustomAlert.show(getWindow(), Alert.AlertType.INFORMATION, "Success", "Server stopped");
        }
    }

    private void setupPlayersList() {
        playersObservableList = FXCollections.observableArrayList();

        FilteredList<PlayerModel> filteredData = new FilteredList<>(playersObservableList, b -> true);

        searchPlayerTextField.textProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue == null || newValue.isEmpty()) {
                emptyTitleLabel.setText("No Players Found");
                emptyHintLabel.setText("Waiting for connections...");
            } else {
                emptyTitleLabel.setText("No Match Found");
                emptyHintLabel.setText("Try searching for a different name");
            }

            filteredData.setPredicate(player -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return player.getUsername().toLowerCase().contains(lowerCaseFilter);
            });
        });

        playersList.setItems(filteredData);
        playersList.setCellFactory(param -> new ListCell<PlayerModel>() {
            @Override
            protected void updateItem(PlayerModel player, boolean empty) {
                super.updateItem(player, empty);

                if (empty || player == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    Label nameLabel = new Label(player.getUsername());
                    nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

                    Label scoreLabel = new Label("Score: " + player.getScore());
                    scoreLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 11px;");

                    VBox vBox = new VBox(3, nameLabel, scoreLabel);

                    Circle statusCircle = new Circle(5);
                    String status = player.getStatus() != null ? player.getStatus().toLowerCase() : "offline";

                    switch (status) {
                        case "active", "online" ->
                            statusCircle.setFill(Color.LIMEGREEN);
                        case "in_game" ->
                            statusCircle.setFill(Color.GOLD);
                        default ->
                            statusCircle.setFill(Color.GREY);
                    }

                    HBox hBox = new HBox(vBox, statusCircle);
                    hBox.setAlignment(Pos.CENTER_LEFT);
                    hBox.setSpacing(10);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    hBox.getChildren().add(1, spacer);

                    setGraphic(hBox);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 5px;");
                }
            }
        });
    }

    private void setupPlaceholder() {
        emptyTitleLabel = new Label("No Players Found");
        emptyTitleLabel.setStyle("-fx-text-fill: #999999; -fx-font-size: 16px; -fx-font-weight: bold;");

        emptyHintLabel = new Label("Waiting for connections...");
        emptyHintLabel.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px;");

        VBox emptyContainer = new VBox(5, emptyTitleLabel, emptyHintLabel);
        emptyContainer.setAlignment(Pos.CENTER);

        playersList.setPlaceholder(emptyContainer);
    }

    private void startListUpdater() {
        Thread updaterThread = new Thread(() -> {
            while (keepUpdating) {
                try {
                    if (UserDAO.getInstance().getConnection() != null) {
                        List<PlayerModel> players = UserDAO.getInstance().getAllPlayers();

                        Platform.runLater(() -> {
                            playersObservableList.setAll(players);
                            updateCounters(players);
                        });
                    }
                    Thread.sleep(3000);
                } catch (InterruptedException | SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        updaterThread.setDaemon(true);
        updaterThread.start();
    }

    private void updateCounters(List<PlayerModel> players) {
        if (activeCountLabel == null) {
            return;
        }

        long active = players.stream()
                .filter(p -> "active".equalsIgnoreCase(p.getStatus()) || "online".equalsIgnoreCase(p.getStatus()))
                .count();
        long inGame = players.stream().filter(p -> "in_game".equalsIgnoreCase(p.getStatus())).count();
        long offline = players.stream()
                .filter(p -> "disactive".equalsIgnoreCase(p.getStatus()) || p.getStatus() == null).count();

        activeCountLabel.setText(String.valueOf(active));
        inGameCountLabel.setText(String.valueOf(inGame));
        offlineCountLabel.setText(String.valueOf(offline));
    }
}
