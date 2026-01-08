package com.mycompany.finalprojectclient.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ResourceBundle;
import com.mycompany.finalprojectclient.utils.NavigationManager;
import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.models.GameSession;
import com.mycompany.finalprojectclient.utils.AuthManager;

public class GameHistoryController implements Initializable {
    
    public static class GameRecord {
        private String fileName;
        private String player1;
        private String player2;
        private String winner;
        private String date;

        public GameRecord(String fileName, String player1, String player2, String winner, String date) {
            this.fileName = fileName;
            this.player1 = player1;
            this.player2 = player2;
            this.winner = winner;
            this.date = date;
        }

        public String getFileName() { return fileName; }
        public String getPlayer1() { return player1; }
        public String getPlayer2() { return player2; }
        public String getWinner() { return winner; }
        public String getDate() { return date; }
    }

    @FXML
    private TableView<GameRecord> historyTable;
    @FXML
    private TableColumn<GameRecord, String> colPlayer1;
    @FXML
    private TableColumn<GameRecord, String> colPlayer2;
    @FXML
    private TableColumn<GameRecord, String> colWinner;
    @FXML
    private TableColumn<GameRecord, String> colDateTime;

    private ObservableList<GameRecord> gameRecords = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colPlayer1.setCellValueFactory(new PropertyValueFactory<>("player1"));
        colPlayer2.setCellValueFactory(new PropertyValueFactory<>("player2"));
        colWinner.setCellValueFactory(new PropertyValueFactory<>("winner"));
        colDateTime.setCellValueFactory(new PropertyValueFactory<>("date"));
        loadHistory();
    }

    private void loadHistory() {
        File folder = new File("history");
        if (!folder.exists()) return;

        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) return;

        for (File file : files) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String playersLine = reader.readLine(); 
                String dateLine = reader.readLine();
                String resultLine = reader.readLine();

                String player1 = "?";
                String player2 = "?";
                String winner = "-";
                String date = "";

                if (playersLine != null && playersLine.contains(" vs ")) {
                    String[] parts = playersLine.split(" vs ");
                    if (parts.length >= 2) {
                        player1 = parts[0];
                        player2 = parts[1];
                    }
                }
                
                String currentUser = AuthManager.getInstance().getCurrentUsername();
                if (currentUser != null && !currentUser.isEmpty()) {
                     boolean isMyMatch = (player1.contains(currentUser) || player2.contains(currentUser));
                     boolean isGeneric = player1.contains("You") || player1.contains("Player 1");
                     if (!isMyMatch && !isGeneric) continue;
                }

                if (dateLine != null && dateLine.startsWith("Date: ")) {
                    date = dateLine.substring(6);
                }
                
                if (resultLine != null && resultLine.startsWith("Result: ")) {
                    winner = resultLine.substring(8);
                } else if (resultLine != null && !resultLine.startsWith("Result:")) {
                }

                gameRecords.add(new GameRecord(file.getAbsolutePath(), player1, player2, winner, date));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        historyTable.setItems(gameRecords);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            NavigationManager.switchScene(event, AppConstants.PATH_GAME_LOBBY);
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
            GameSession.isOnline = false;
            GameSession.vsComputer = false;
            try {
                NavigationManager.switchScene(event, AppConstants.PATH_GAME_BOARD);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
