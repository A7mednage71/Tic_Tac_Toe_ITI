package com.mycompany.finalprojectclient.controllers;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import com.mycompany.finalprojectclient.models.GameSession;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

public class BoardController implements Initializable {

    @FXML
    private GridPane gameGrid;
    @FXML
    private Label scoreX;
    @FXML
    private Label scoreO;
    @FXML
    private Label playerNameLabel;
    @FXML
    private Label opponentNameLabel;
    @FXML
    private StackPane drawOverlay;
    @FXML
    private StackPane videoOverlay;
    @FXML
    private MediaView videoMediaView;

    private MediaPlayer mediaPlayer;

    private String playerX = "You";
    private String playerO = "Opponent";
    private int countX = 0;
    private int countO = 0;
    private boolean isXTurn = true;
    private boolean gameOver = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (GameSession.isOnline) {
            String myName = com.mycompany.finalprojectclient.utils.AuthManager.getInstance().getCurrentUsername();
            if ("X".equals(GameSession.playerSymbol)) {
                playerX = myName;
                playerO = GameSession.opponentName;
                playerNameLabel.setText(playerX + " (X)");
                opponentNameLabel.setText(playerO + " (O)");
            } else {
                playerX = GameSession.opponentName;
                playerO = myName;
                playerNameLabel.setText(playerO + " (O)");
                opponentNameLabel.setText(playerX + " (X)");
            }

            // Setup listener for opponent withdrawal
            com.mycompany.finalprojectclient.network.ServerConnection.getInstance().setInviteListener(new com.mycompany.finalprojectclient.network.ServerConnection.InviteListener() {
                @Override public void onInviteReceived(String from) {}
                @Override public void onInviteAccepted(String user) {}
                @Override public void onInviteRejected(String user) {}
                
                @Override
                public void onOpponentWithdrew(String username) {
                    Platform.runLater(() -> {
                        if (!gameOver) {
                            // Update score for the winner who stayed
                            if ("X".equals(GameSession.playerSymbol)) countX++; else countO++;
                            updateScoreLabels();
                            finishGame("Opponent Left. You Win!");
                        }
                    });
                }
            });

        } else if (GameSession.vsComputer) {
            playerX = "You";
            playerO = "Computer";
            playerNameLabel.setText("You (X)");
            opponentNameLabel.setText("Computer 💻");
        } else {
            playerX = "Player 1";
            playerO = "Player 2";
            playerNameLabel.setText("Player 1 (X)");
            opponentNameLabel.setText("Player 2 (O)");
        }
        updateScoreLabels();
    }

    @FXML
    private void handleCellClick(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        if (clickedButton.getText().isEmpty() && !gameOver) {
            if (GameSession.vsComputer) {
                playMove(clickedButton, "X");
                if (checkWinner("X")) {
                    countX++;
                    finishGame("X Wins!");
                } else if (isBoardFull()) {
                    finishGame("Draw!");
                } else {
                    gameGrid.setDisable(true);
                    playComputerTurn();
                }
            } else {
                String currentSymbol = isXTurn ? "X" : "O";
                playMove(clickedButton, currentSymbol);
                if (checkWinner(currentSymbol)) {
                    if (isXTurn)
                        countX++;
                    else
                        countO++;
                    finishGame(currentSymbol + " Wins!");
                } else if (isBoardFull()) {
                    finishGame("Draw!");
                } else {
                    isXTurn = !isXTurn;
                    opponentNameLabel.setText(isXTurn ? "Player 1's Turn (X)" : "Player 2's Turn (O)");
                }
            }
        }
    }

    private void playComputerTurn() {
        new Thread(() -> {
            try {
                Thread.sleep(600);
            } catch (InterruptedException e) {
            }
            Platform.runLater(() -> {
                gameGrid.setDisable(false);
                if (!gameOver) {
                    makeAiMove();
                    if (checkWinner("O")) {
                        countO++;
                        finishGame("O Wins!");
                    } else if (isBoardFull()) {
                        finishGame("Draw!");
                    }
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
                if (!smartMove("X"))
                    easyMove();
                break;
            case HARD:
                if (smartMove("O"))
                    return;
                if (smartMove("X"))
                    return;
                if (takeCenter())
                    return;
                if (takeCorner())
                    return;
                easyMove();
                break;
        }
    }

    private boolean smartMove(String symbol) {
        Button[][] grid = getGridArray();
        for (int i = 0; i < 3; i++) {
            if (findEmptyInLine(grid[i][0], grid[i][1], grid[i][2], symbol))
                return true;
            if (findEmptyInLine(grid[0][i], grid[1][i], grid[2][i], symbol))
                return true;
        }
        if (findEmptyInLine(grid[0][0], grid[1][1], grid[2][2], symbol))
            return true;
        if (findEmptyInLine(grid[0][2], grid[1][1], grid[2][0], symbol))
            return true;
        return false;
    }

    private boolean findEmptyInLine(Button b1, Button b2, Button b3, String symbol) {
        if (b1.getText().equals(symbol) && b2.getText().equals(symbol) && b3.getText().isEmpty()) {
            playMove(b3, "O");
            return true;
        }
        if (b1.getText().equals(symbol) && b3.getText().equals(symbol) && b2.getText().isEmpty()) {
            playMove(b2, "O");
            return true;
        }
        if (b2.getText().equals(symbol) && b3.getText().equals(symbol) && b1.getText().isEmpty()) {
            playMove(b1, "O");
            return true;
        }
        return false;
    }

    private Button[][] getGridArray() {
        Button[][] grid = new Button[3][3];
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                int r = GridPane.getRowIndex(btn) == null ? 0 : GridPane.getRowIndex(btn);
                int c = GridPane.getColumnIndex(btn) == null ? 0 : GridPane.getColumnIndex(btn);
                grid[r][c] = btn;
            }
        }
        return grid;
    }

    private void easyMove() {
        List<Button> emptyButtons = new ArrayList<>();
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof Button && ((Button) node).getText().isEmpty()) {
                emptyButtons.add((Button) node);
            }
        }
        if (!emptyButtons.isEmpty()) {
            playMove(emptyButtons.get(new Random().nextInt(emptyButtons.size())), "O");
        }
    }

    private void playMove(Button btn, String symbol) {
        btn.setText(symbol);
        btn.getStyleClass().removeAll("cell-button-empty");
        btn.getStyleClass().add(symbol.equals("X") ? "cell-button-x" : "cell-button-o");
        btn.setStyle("-fx-background-color: white;");
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            Platform.runLater(() -> btn.setStyle(""));
        }).start();
    }

    private void finishGame(String message) {
        gameOver = true;
        updateScoreLabels();
        Platform.runLater(() -> {
            String msgLower = message.toLowerCase();
            if (msgLower.contains("win")) {
                boolean showWinnerVideo = true;

                if (GameSession.isOnline) {
                    // In online mode, check if the winning symbol matches our symbol
                    // message could be "X Wins!", "O Wins!", or "Opponent Left. You Win!"
                    if (message.contains("X Wins") && !"X".equals(GameSession.playerSymbol)) {
                        showWinnerVideo = false;
                    } else if (message.contains("O Wins") && !"O".equals(GameSession.playerSymbol)) {
                        showWinnerVideo = false;
                    }
                    // If message contains "You Win" (from withdrawal), showWinnerVideo stays true
                } else if (GameSession.vsComputer) {
                    // In vsComputer, player is always X
                    if (message.contains("O Wins")) {
                        showWinnerVideo = false;
                    }
                }
                // For Local PVP, we'll keep showing winner video as both players are at the same screen

                if (showWinnerVideo) {
                    playVideo("/videos/winner.mp4");
                } else {
                    playVideo("/videos/loser.mp4");
                }
            } else if (msgLower.contains("draw")) {
                showDrawAlert();
            } else {
                showDrawAlert();
            }
        });
    }

    private void showDrawAlert() {
        drawOverlay.setVisible(true);
        drawOverlay.setManaged(true);
        drawOverlay.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(500), drawOverlay);
        ft.setToValue(1.0);
        ft.play();
    }

    @FXML
    private void handlePlayAgainFromOverlay(ActionEvent event) {
        drawOverlay.setVisible(false);
        drawOverlay.setManaged(false);
        handlePlayAgain(event);
    }

    private void highlightWinningButtons(Button... buttons) {
        for (Button btn : buttons)
            btn.getStyleClass().add("winning-button");
    }

    private boolean checkWinner(String symbol) {
        Button[][] grid = getGridArray();
        for (int i = 0; i < 3; i++) {
            if (symbol.equals(grid[i][0].getText()) && symbol.equals(grid[i][1].getText())
                    && symbol.equals(grid[i][2].getText())) {
                highlightWinningButtons(grid[i][0], grid[i][1], grid[i][2]);
                return true;
            }
            if (symbol.equals(grid[0][i].getText()) && symbol.equals(grid[1][i].getText())
                    && symbol.equals(grid[2][i].getText())) {
                highlightWinningButtons(grid[0][i], grid[1][i], grid[2][i]);
                return true;
            }
        }
        if (symbol.equals(grid[0][0].getText()) && symbol.equals(grid[1][1].getText())
                && symbol.equals(grid[2][2].getText())) {
            highlightWinningButtons(grid[0][0], grid[1][1], grid[2][2]);
            return true;
        }
        if (symbol.equals(grid[0][2].getText()) && symbol.equals(grid[1][1].getText())
                && symbol.equals(grid[2][0].getText())) {
            highlightWinningButtons(grid[0][2], grid[1][1], grid[2][0]);
            return true;
        }
        return false;
    }

    private boolean isBoardFull() {
        for (Node node : gameGrid.getChildren()) {
            if (node instanceof Button && ((Button) node).getText().isEmpty())
                return false;
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
                if (!btn.getStyleClass().contains("cell-button-empty"))
                    btn.getStyleClass().add("cell-button-empty");
            }
        }
        gameOver = false;
        isXTurn = true;
        if (!GameSession.vsComputer)
            opponentNameLabel.setText("Player 1's Turn (X)");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            if (GameSession.isOnline) {
                // Send WITHDRAW to server
                com.mycompany.finalprojectclient.models.RequestData req = new com.mycompany.finalprojectclient.models.RequestData();
                req.key = com.mycompany.finalprojectclient.models.RequestType.WITHDRAW;
                req.username = com.mycompany.finalprojectclient.utils.AuthManager.getInstance().getCurrentUsername();
                req.targetUsername = GameSession.opponentName;
                com.mycompany.finalprojectclient.network.ServerConnection.getInstance().sendRequest(req);

                GameSession.isOnline = false;
                switchScene("TicTacToeLobby.fxml", event);
            } else {
                switchScene(GameSession.vsComputer ? "vsComputer.fxml" : "Home.fxml", event);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
            if (videoUrl == null) return;
            
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            Media media = new Media(videoUrl.toExternalForm());
            mediaPlayer = new MediaPlayer(media);
            videoMediaView.setMediaPlayer(mediaPlayer);
            
            videoOverlay.setVisible(true);
            videoOverlay.setManaged(true);
            videoOverlay.setOpacity(0);
            
            FadeTransition ft = new FadeTransition(Duration.millis(500), videoOverlay);
            ft.setToValue(1.0);
            ft.play();

            mediaPlayer.play();
            mediaPlayer.setOnEndOfMedia(() -> {
                // Keep the last frame or show button
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCloseVideo(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        
        FadeTransition ft = new FadeTransition(Duration.millis(300), videoOverlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            videoOverlay.setVisible(false);
            videoOverlay.setManaged(false);
        });
        ft.play();
    }

    private boolean takeCenter() {
        Button[][] grid = getGridArray();
        if (grid[1][1].getText().isEmpty()) {
            playMove(grid[1][1], "O");
            return true;
        }
        return false;
    }

    private boolean takeCorner() {
        Button[][] grid = getGridArray();
        int[][] corners = { { 0, 0 }, { 0, 2 }, { 2, 0 }, { 2, 2 } };
        List<Button> availableCorners = new ArrayList<>();
        for (int[] corner : corners) {
            if (grid[corner[0]][corner[1]].getText().isEmpty()) {
                availableCorners.add(grid[corner[0]][corner[1]]);
            }
        }
        if (!availableCorners.isEmpty()) {
            playMove(availableCorners.get(new Random().nextInt(availableCorners.size())), "O");
            return true;
        }
        return false;
    }
}