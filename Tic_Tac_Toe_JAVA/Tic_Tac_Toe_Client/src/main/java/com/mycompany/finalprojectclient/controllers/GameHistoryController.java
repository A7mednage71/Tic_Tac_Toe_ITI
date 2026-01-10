package com.mycompany.finalprojectclient.controllers;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.mycompany.finalprojectclient.models.GameRecord;
import com.mycompany.finalprojectclient.models.GameSession;
import com.mycompany.finalprojectclient.models.RequestData;
import com.mycompany.finalprojectclient.models.RequestType;
import com.mycompany.finalprojectclient.network.ServerConnection;
import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.utils.AuthManager;
import com.mycompany.finalprojectclient.utils.NavigationManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class GameHistoryController implements Initializable {

    @FXML
    private TableView<GameRecord> historyTable;
    @FXML
    private TableColumn<GameRecord, String> colGameId;
    @FXML
    private TableColumn<GameRecord, String> colPlayer1;
    @FXML
    private TableColumn<GameRecord, String> colMove1;
    @FXML
    private TableColumn<GameRecord, String> colPlayer2;
    @FXML
    private TableColumn<GameRecord, String> colMove2;
    @FXML
    private TableColumn<GameRecord, String> colWinner;
    @FXML
    private TableColumn<GameRecord, String> colDuration;
    @FXML
    private TableColumn<GameRecord, String> colDateTime;

    private ObservableList<GameRecord> gameRecords = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colGameId.setCellValueFactory(new PropertyValueFactory<>("gameId"));
        colPlayer1.setCellValueFactory(new PropertyValueFactory<>("player1"));
        colMove1.setCellValueFactory(new PropertyValueFactory<>("move1"));
        colPlayer2.setCellValueFactory(new PropertyValueFactory<>("player2"));
        colMove2.setCellValueFactory(new PropertyValueFactory<>("move2"));
        colWinner.setCellValueFactory(new PropertyValueFactory<>("winner"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("formattedDuration"));
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("date"));
        
        updatePlayerStatusToViewingHistory();
        loadHistory();
    }

    private void loadHistory() {
        File folder = new File("history");
        if (!folder.exists())
            return;

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null)
            return;

        Gson gson = new Gson();

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                GameRecord gameRecord = gson.fromJson(reader, GameRecord.class);

                if (gameRecord == null)
                    continue;
                gameRecord.setFileName(file.getAbsolutePath());
                String currentUser = AuthManager.getInstance().getCurrentUsername();
                if (currentUser != null && !currentUser.isEmpty()) {
                    boolean isMyMatch = (gameRecord.getPlayerX().contains(currentUser) ||
                            gameRecord.getPlayerO().contains(currentUser));
                    boolean isGeneric = gameRecord.getPlayerX().contains("You") ||
                            gameRecord.getPlayerX().contains("Player 1");
                    if (!isMyMatch && !isGeneric)
                        continue;
                }
                gameRecords.add(gameRecord);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        historyTable.setItems(gameRecords);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            updatePlayerStatusToOnline();
            
            String targetPath = GameSession.previousScreen.isEmpty() ? AppConstants.PATH_GAME_LOBBY
                    : GameSession.previousScreen;
            NavigationManager.switchScene(event, targetPath);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleReplay(ActionEvent event) {
        GameRecord selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            GameSession.isReplay = true;
            GameSession.replayFilePath = selected.getFileName();
            GameSession.isHistoryReplay = true;
            GameSession.isOnline = false;
            GameSession.vsComputer = false;
            try {
                NavigationManager.switchScene(event, AppConstants.PATH_GAME_BOARD);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
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

    private void updatePlayerStatusToOnline() {
        try {
            String username = AuthManager.getInstance().getCurrentUsername();
            if (username != null && !username.isEmpty()) {
                RequestData request = new RequestData();
                request.key = RequestType.UPDATE_STATUS;
                request.username = username;
                request.status = "online";
                
                try {
                    ServerConnection.getInstance().sendRequest(request);
                    System.out.println("Status update sent to server: online");
                } catch (Exception e) {
                    System.err.println("Error updating status to online: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in updatePlayerStatusToOnline: " + e.getMessage());
        }
    }
}
