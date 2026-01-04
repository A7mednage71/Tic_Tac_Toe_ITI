package com.mycompany.finalprojectclient;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;

public class BoardController implements Initializable {

    @FXML private GridPane gameGrid;
    @FXML private Label scoreX;
    @FXML private Label scoreO;
    @FXML private Label opponentNameLabel;

    private String playerX = "You";
    private String playerO = "Opponent";
    private int countX = 0;
    private int countO = 0;
    private boolean isPlayerTurn = true; 
    private boolean gameOver = false;

   @Override
public void initialize(URL location, ResourceBundle resources) {
    if (GameSession.vsComputer) {
        playerX = "You";
        playerO = "Computer";
        opponentNameLabel.setText("Computer 💻"); 
    } else {
        playerX = "Player 1";
        playerO = "Player 2";
        opponentNameLabel.setText("Player 2"); 
    }
    updateScoreLabels();
}

    @FXML
    private void handleCellClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();

        if (clickedButton.getText().isEmpty() && !gameOver && isPlayerTurn) {
            playMove(clickedButton, "X");
            
            if (checkWinner("X")) {
                countX++;
                finishGame("X Wins!");
            } else if (isBoardFull()) {
                finishGame("Draw!");
            } else if (GameSession.vsComputer) {
                isPlayerTurn = false;
                playComputerTurn();
            }
        }
    }

    private void playComputerTurn() {
        new Thread(() -> {
            try { Thread.sleep(600); } catch (InterruptedException e) {}
            Platform.runLater(() -> {
                if (!gameOver) {
                    makeAiMove();
                    if (checkWinner("O")) {
                        countO++;
                        finishGame("O Wins!");
                    } else if (isBoardFull()) {
                        finishGame("Draw!");
                    }
                    isPlayerTurn = true;
                }
            });
        }).start();
    }

    private void makeAiMove() {
        
        switch (GameSession.difficulty) {
            case EASY:
                easyMove();
                break;
            case MEDIUM:
            case HARD:
               
                easyMove(); 
                break;
        }
    }

    private void easyMove() {
        List<Button> emptyButtons = new ArrayList<>();
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                if (btn.getText().isEmpty()) emptyButtons.add(btn);
            }
        }
        if (!emptyButtons.isEmpty()) {
            playMove(emptyButtons.get(new Random().nextInt(emptyButtons.isEmpty() ? 0 : emptyButtons.size())), "O");
        }
    }

    private void playMove(Button btn, String symbol) {
       btn.setText(symbol);
    btn.getStyleClass().removeAll("cell-button-empty");
    btn.getStyleClass().add(symbol.equals("X") ? "cell-button-x" : "cell-button-o");
    
  
    btn.setStyle("-fx-background-color: white;"); 
    new Thread(() -> {
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        Platform.runLater(() -> btn.setStyle("")); // بيرجع للـ CSS الأصلي
    }).start();
}
       
    

  private void finishGame(String message) {
    gameOver = true;
    updateScoreLabels();

    Platform.runLater(() -> {
        if (message.contains("X Wins")) {

            playVideo("/videos/winner.mp4");
        } else if (message.contains("O Wins")) {
            
            playVideo("/videos/loser.mp4");
        } else {
            
            System.out.println("It's a draw!");
        }
    });
}

   private void highlightWinningButtons(Button... buttons) {
    for (Button btn : buttons) {
        btn.getStyleClass().add("winning-button");
    }
}

private boolean checkWinner(String symbol) {
    Button[][] grid = new Button[3][3];
    for (Node node : gameGrid.getChildren()) {
        if (node instanceof Button) {
            Button btn = (Button) node;
            int r = GridPane.getRowIndex(btn) == null ? 0 : GridPane.getRowIndex(btn);
            int c = GridPane.getColumnIndex(btn) == null ? 0 : GridPane.getColumnIndex(btn);
            grid[r][c] = btn;
        }
    }

    
    for (int i = 0; i < 3; i++) {
        if (symbol.equals(grid[i][0].getText()) && symbol.equals(grid[i][1].getText()) && symbol.equals(grid[i][2].getText())) {
            highlightWinningButtons(grid[i][0], grid[i][1], grid[i][2]);
            return true;
        }
        if (symbol.equals(grid[0][i].getText()) && symbol.equals(grid[1][i].getText()) && symbol.equals(grid[2][i].getText())) {
            highlightWinningButtons(grid[0][i], grid[1][i], grid[2][i]);
            return true;
        }
    }
 
    if (symbol.equals(grid[0][0].getText()) && symbol.equals(grid[1][1].getText()) && symbol.equals(grid[2][2].getText())) {
        highlightWinningButtons(grid[0][0], grid[1][1], grid[2][2]);
        return true;
    }
    if (symbol.equals(grid[0][2].getText()) && symbol.equals(grid[1][1].getText()) && symbol.equals(grid[2][0].getText())) {
        highlightWinningButtons(grid[0][2], grid[1][1], grid[2][0]);
        return true;
    }
    return false;
}

    private boolean isBoardFull() {
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof Button && ((Button) node).getText().isEmpty()) return false;
        }
        return true;
    }

    @FXML
    
private void handlePlayAgain(ActionEvent event) {
    for (Node node : gameGrid.getChildren()) {
        if (node instanceof Button) {
            Button btn = (Button) node;
            
        
            btn.setText("");
            
          
            btn.getStyleClass().removeAll("cell-button-x", "cell-button-o", "winning-button");
            
            
            if (!btn.getStyleClass().contains("cell-button-empty")) {
                btn.getStyleClass().add("cell-button-empty");
            }
        }
    }
 
    gameOver = false;
    isPlayerTurn = true;
}
    @FXML
    private void handleBack(ActionEvent event) {
        try { switchScene("vsComputer.fxml", event); } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateScoreLabels() {
        scoreX.setText(playerX + " (X): " + countX);
        scoreO.setText(playerO + " (O): " + countO);
    }

    private void switchScene(String fxml, ActionEvent e) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/finalprojectclient/" + fxml));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
   private void playVideo(String videoPath) {
    try {
        URL videoUrl = getClass().getResource(videoPath);
        if (videoUrl == null) {
            System.out.println("Video file not found: " + videoPath);
            return;
        }

        Media media = new Media(videoUrl.toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        MediaView mediaView = new MediaView(mediaPlayer);

        mediaView.setFitWidth(600); 
        mediaView.setPreserveRatio(true);

        StackPane root = new StackPane(mediaView);
        Scene scene = new Scene(root, 600, 400);
        Stage videoStage = new Stage();
        videoStage.setTitle(videoPath.contains("winner") ? "🎉 You Won! 🎉" : "😢 Hard Luck! 😢");
        videoStage.setScene(scene);

        videoStage.show();
        mediaPlayer.play();

        mediaPlayer.setOnEndOfMedia(() -> {
            mediaPlayer.dispose(); 
            videoStage.close(); 
        });

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}