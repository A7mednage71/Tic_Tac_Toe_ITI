package com.mycompany.finalprojectclient.controllers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mycompany.finalprojectclient.MainApp;
import com.mycompany.finalprojectclient.models.GameRecord;
import com.mycompany.finalprojectclient.models.GameSession;
import com.mycompany.finalprojectclient.models.RequestData;
import com.mycompany.finalprojectclient.models.RequestType;
import com.mycompany.finalprojectclient.network.ServerConnection;
import com.mycompany.finalprojectclient.utils.AppConstants;
import com.mycompany.finalprojectclient.utils.AuthManager;
import com.mycompany.finalprojectclient.utils.CustomAlertHandler;
import com.mycompany.finalprojectclient.utils.NavigationManager;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
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
    private StackPane videoOverlay;
    @FXML
    private MediaView videoMediaView;
    @FXML
    private Label gameResultLabel;
    @FXML
    private StackPane customAlertOverlay;
    @FXML
    private VBox alertBox;
    @FXML
    private Label alertTitle;
    @FXML
    private Label alertMessage;
    @FXML
    private Label alertIcon;

    private CustomAlertHandler alertHandler;
    private MediaPlayer mediaPlayer;
    private String gameFinishMessage;
    @FXML
    private Circle recordingIndicator;
    @FXML
    private Button recordBtn;
    @FXML
    private Label timerLabel;

    private boolean isRecording = false;
    private javafx.animation.Timeline gameTimer;
    private int elapsedSeconds = 0;
    private List<String> recordedMoves = new ArrayList<>();
    private FadeTransition blinkingAnimation;
    private boolean gameStarted = false;

    private String playerX = "You";
    private String playerO = "Opponent";
    private String leftPlayer = null; // اللاعب على الشمال (playerNameLabel)
    private String rightPlayer = null; // اللاعب على اليمين (opponentNameLabel)
    private int countX = 0;
    private int countO = 0;
    private boolean isXTurn = true;
    private boolean gameOver = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("BoardController Initializing...");
        System.out.println("videoOverlay injected: " + (videoOverlay != null));
        System.out.println("videoMediaView injected: " + (videoMediaView != null));

        com.mycompany.finalprojectclient.network.ServerConnection.getInstance().setNotificationListener(null);

        alertHandler = new CustomAlertHandler(customAlertOverlay, alertBox, alertTitle, alertMessage, alertIcon);
        updateBoardHoverState();

        // Listener للاستماع لتحديثات الـ scores من السيرفر
        ServerConnection.getInstance().setScoreListener((username, newScore) -> {
            Platform.runLater(() -> {
                updatePlayerScore(username, newScore);
            });
        });

        if (GameSession.isOnline) {
            String myName = AuthManager.getInstance().getCurrentUsername();
            System.out.println("=== BoardController Initialize - Online Game ===");
            System.out.println("My username: " + myName);
            System.out.println("My symbol: " + GameSession.playerSymbol);
            System.out.println("Opponent name: " + GameSession.opponentName);

            if ("X".equals(GameSession.playerSymbol)) {
                playerX = myName;
                playerO = GameSession.opponentName;
                playerNameLabel.setText(playerX + " (X)");
                opponentNameLabel.setText(playerO + " (O)");
                // playerX على الشمال، playerO على اليمين
                leftPlayer = playerX;
                rightPlayer = playerO;
            } else {
                playerX = GameSession.opponentName;
                playerO = myName;
                playerNameLabel.setText(playerO + " (O)");
                opponentNameLabel.setText(playerX + " (X)");
                // playerO على الشمال، playerX على اليمين
                leftPlayer = playerO;
                rightPlayer = playerX;
            }

            System.out.println("leftPlayer (scoreX position): " + leftPlayer);
            System.out.println("rightPlayer (scoreO position): " + rightPlayer);

            ServerConnection.getInstance()
                    .setInviteListener(new ServerConnection.InviteListener() {
                        @Override
                        public void onInviteReceived(String from) {
                        }

                        @Override
                        public void onInviteAccepted(String user) {
                            Platform.runLater(() -> {
                                alertHandler.hide();
                            });
                        }

                        @Override
                        public void onInviteRejected(String user) {
                            Platform.runLater(() -> {
                                alertHandler.showError("DECLINED", user + " doesn't want to play again.");
                            });
                        }

                        @Override
                        public void onPlayAgainRequested(String username) {
                            Platform.runLater(() -> {
                                alertHandler.showConfirmation("REMATCH CHALLENGE",
                                        username + " wants a rematch! Accept?",
                                        new CustomAlertHandler.ConfirmationCallback() {
                                            @Override
                                            public void onYes() {
                                                acceptOnlineRematch();
                                            }

                                            @Override
                                            public void onNo() {
                                                rejectOnlineRematch();
                                            }
                                        });
                            });
                        }

                        @Override
                        public void onOpponentWithdrew(String username) {
                            Platform.runLater(() -> {
                                if (gameOver) {
                                    alertHandler.showError("LEFT", username + " has left.");
                                    return;
                                }
                                if ("X".equals(GameSession.playerSymbol))
                                    countX++;
                                else
                                    countO++;
                                finishGame("Opponent Left. You Win!");
                            });
                        }

                        @Override
                        public void onGameStart(String symbol, String opponent) {
                            Platform.runLater(() -> {
                                GameSession.playerSymbol = symbol;
                                alertHandler.hide();
                                updateUserStatus(AuthManager.getInstance()
                                        .getCurrentUsername(), "in_game");
                                resetBoard();
                                updateScoreLabels();

                                if (!GameSession.isOnline) {
                                    recordBtn.setVisible(false);
                                    recordBtn.setManaged(false);
                                } else {
                                    recordBtn.setVisible(true);
                                    recordBtn.setManaged(true);
                                }
                            });
                        }
                    });

            ServerConnection.getInstance().setGameMoveListener((r, c) -> {
                Platform.runLater(() -> {
                    Button[][] grid = getGridArray();
                    if (r >= 0 && r < 3 && c >= 0 && c < 3 && grid[r][c].getText().isEmpty()) {
                        String oppSymbol = "X".equals(GameSession.playerSymbol) ? "O" : "X";
                        playMove(grid[r][c], oppSymbol);
                        if (checkWinner(oppSymbol)) {
                            if (oppSymbol.equals("X"))
                                countX++;
                            else
                                countO++;
                            finishGame(oppSymbol + " Wins!");
                        } else if (isBoardFull()) {
                            finishGame("Draw!");
                        } else {
                            isXTurn = !isXTurn;
                            updateBoardHoverState();
                        }
                    }
                });
            });

        } else if (GameSession.vsComputer) {
            playerX = "You";
            playerO = "Computer";
            playerNameLabel.setText("You (X)");
            opponentNameLabel.setText("Computer 💻");
        } else if (GameSession.isReplay) {
            loadReplay(GameSession.replayFilePath);
        } else {
            playerX = "Player 1";
            playerO = "Player 2";
            playerNameLabel.setText("Player 1 (X)");
            opponentNameLabel.setText("Player 2 (O)");
        }
        updateScoreLabels();

        if (!GameSession.isOnline) {
            recordBtn.setVisible(false);
            recordBtn.setManaged(false);
        } else {
            recordBtn.setVisible(true);
            recordBtn.setManaged(true);
        }
    }

    private void loadReplay(String path) {
        gameOver = true;
        new Thread(() -> {
            try (FileReader reader = new FileReader(path)) {
                Gson gson = new Gson();
                GameRecord gameRecord = gson.fromJson(reader, GameRecord.class);

                if (gameRecord == null) {
                    Platform.runLater(() -> {
                        showResultOverlay("❌", "FAILED TO LOAD REPLAY");
                        GameSession.isReplay = false;
                    });
                    return;
                }

                Platform.runLater(() -> {
                    playerX = gameRecord.getPlayerX();
                    playerO = gameRecord.getPlayerO();
                    // في الـ replay، نعرض الأسماء مع الـ symbols بشكل ثابت
                    playerNameLabel.setText(playerX + " (X)");
                    opponentNameLabel.setText(playerO + " (O)");
                    updateScoreLabels();

                    if (!GameSession.isOnline) {
                        recordBtn.setVisible(false);
                        recordBtn.setManaged(false);
                    } else {
                        recordBtn.setVisible(true);
                        recordBtn.setManaged(true);
                    }
                });

                List<String> moves = gameRecord.getMoves();
                if (moves != null) {
                    for (String move : moves) {
                        if (move.contains(",")) {
                            String[] parts = move.split(",");
                            if (parts.length >= 3) {
                                int r = Integer.parseInt(parts[0].trim());
                                int c = Integer.parseInt(parts[1].trim());
                                String sym = parts[2].trim();

                                Thread.sleep(800);
                                Platform.runLater(() -> {
                                    Button[][] grid = getGridArray();
                                    playMove(grid[r][c], sym);
                                });
                            }
                        }
                    }
                }

                Thread.sleep(1500);
                Platform.runLater(() -> {
                    showResultOverlay("🎬", "REPLAY ENDED");
                    GameSession.isReplay = false;
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showResultOverlay("❌", "REPLAY ERROR");
                    GameSession.isReplay = false;
                });
            }
        }).start();
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
            } else if (GameSession.isOnline) {
                String mySymbol = GameSession.playerSymbol;
                boolean isMyTurn = (isXTurn && "X".equals(mySymbol)) || (!isXTurn && "O".equals(mySymbol));

                if (isMyTurn) {
                    playMove(clickedButton, mySymbol);

                    int r = GridPane.getRowIndex(clickedButton) == null ? 0 : GridPane.getRowIndex(clickedButton);
                    int c = GridPane.getColumnIndex(clickedButton) == null ? 0 : GridPane.getColumnIndex(clickedButton);
                    ServerConnection.getInstance().sendGameMove(r, c);

                    if (checkWinner(mySymbol)) {
                        if (mySymbol.equals("X"))
                            countX++;
                        else
                            countO++;
                        finishGame(mySymbol + " Wins!");
                    } else if (isBoardFull()) {
                        finishGame("Draw!");
                    } else {
                        isXTurn = !isXTurn;
                        updateBoardHoverState();
                    }
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
                    updateBoardHoverState();
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
                    } else {
                        isXTurn = !isXTurn;
                        updateBoardHoverState();
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
        int r = GridPane.getRowIndex(btn) == null ? 0 : GridPane.getRowIndex(btn);
        int c = GridPane.getColumnIndex(btn) == null ? 0 : GridPane.getColumnIndex(btn);

        // إخفاء زرار Record نهائياً عند أول move إذا لم يكن التسجيل مفعل وبدء التايمر
        if (!gameStarted) {
            gameStarted = true;
            startGameTimer();
            if (!isRecording && recordBtn != null) {
                recordBtn.setVisible(false);
                recordBtn.setManaged(false);
            }
        }

        if (isRecording) {
            recordedMoves.add(r + "," + c + "," + symbol);
        }

        btn.setText(symbol);
        btn.getStyleClass().removeAll("cell-button-empty");
        btn.getStyleClass().add(symbol.equals("X") ? "cell-button-x" : "cell-button-o");
        new Thread(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
            Platform.runLater(() -> btn.setStyle(""));
        }).start();
    }

    @FXML
    private void handleRecord(ActionEvent event) {
        // لو التسجيل شغال، نوقفه
        if (isRecording) {
            stopRecording();
            return;
        }

        // بدء التسجيل
        isRecording = true;
        recordBtn.setText("Stop Rec");
        recordBtn.setStyle("-fx-background-color: #c0392b;");
        recordingIndicator.setVisible(true);
        blinkingAnimation = new FadeTransition(Duration.millis(500), recordingIndicator);
        blinkingAnimation.setFromValue(1.0);
        blinkingAnimation.setToValue(0.1);
        blinkingAnimation.setCycleCount(Animation.INDEFINITE);
        blinkingAnimation.setAutoReverse(true);
        blinkingAnimation.play();
    }

    private void stopRecording() {
        if (!isRecording)
            return;
        isRecording = false;
        recordBtn.setText("Record");
        recordBtn.setStyle("-fx-background-color: #e74c3c;");
        recordingIndicator.setVisible(false);
        if (blinkingAnimation != null) {
            blinkingAnimation.stop();
        }
        saveGameHistory();
    }

    private void saveGameHistory() {
        if (recordedMoves.isEmpty())
            return;
        try {
            File dir = new File("history");
            if (!dir.exists())
                dir.mkdir();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String gameId = "GAME_" + timestamp;
            String filename = "history/game_" + timestamp + ".json";

            String winner = "-";
            if (gameFinishMessage != null) {
                if (gameFinishMessage.contains("X Wins")) {
                    winner = playerX;
                } else if (gameFinishMessage.contains("O Wins")) {
                    winner = playerO;
                } else if (gameFinishMessage.contains("Draw")) {
                    winner = "Draw";
                } else if (gameFinishMessage.contains("You Win")) {
                    // في حالة الفوز بسبب انسحاب الخصم
                    String myName = AuthManager.getInstance().getCurrentUsername();
                    winner = myName;
                } else if (gameFinishMessage.contains("Opponent Left") || gameFinishMessage.contains("Opponent Wins")) {
                    // في حالة انسحاب الخصم (أنت فزت) أو انسحابك أنت (الخصم فاز)
                    if (gameFinishMessage.contains("Opponent Wins") || gameFinishMessage.contains("You Left")) {
                        // أنت انسحبت، الخصم فاز
                        winner = GameSession.opponentName;
                    } else {
                        // الخصم انسحب، أنت فزت
                        String myName = AuthManager.getInstance().getCurrentUsername();
                        winner = myName;
                    }
                }
            }

            GameRecord gameRecord = new GameRecord(
                    gameId, playerX, playerO, winner,
                    LocalDateTime.now().toString(), recordedMoves, elapsedSeconds);

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            try (FileWriter writer = new FileWriter(filename)) {
                gson.toJson(gameRecord, writer);
            }

            System.out.println("Game recorded to: " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void finishGame(String message) {
        gameOver = true;
        gameFinishMessage = message;
        stopGameTimer();
        updateBoardHoverState();
        updateScoreLabels();

        // إرسال نتيجة اللعبة للسيرفر لتحديث الـ scores
        if (GameSession.isOnline) {
            sendGameEndToServer(message);
        }

        // تحويل الرسالة لرسالة واضحة وشخصية للاعب
        final String displayMessage;
        if (GameSession.isOnline) {
            if (message.contains("X Wins")) {
                if ("X".equals(GameSession.playerSymbol)) {
                    displayMessage = "You Won! 🎉 (+10 points)";
                } else {
                    displayMessage = "You Lost! 😔 (-5 points)";
                }
            } else if (message.contains("O Wins")) {
                if ("O".equals(GameSession.playerSymbol)) {
                    displayMessage = "You Won! 🎉 (+10 points)";
                } else {
                    displayMessage = "You Lost! 😔 (-5 points)";
                }
            } else if (message.toLowerCase().contains("draw")) {
                displayMessage = "Draw! 🤝 (+3 points)";
            } else if (message.toLowerCase().contains("left")) {
                displayMessage = "You Won! 🎉 Opponent Left (+10 points)";
            } else {
                displayMessage = message;
            }
        } else {
            displayMessage = message;
        }

        String msgLower = message.toLowerCase();
        if (msgLower.contains("win")) {
            boolean showWinnerVideo = true;
            if (GameSession.isOnline) {
                if (message.contains("X Wins") && !"X".equals(GameSession.playerSymbol))
                    showWinnerVideo = false;
                else if (message.contains("O Wins") && !"O".equals(GameSession.playerSymbol))
                    showWinnerVideo = false;
            } else if (GameSession.vsComputer) {
                if (message.contains("O Wins"))
                    showWinnerVideo = false;
            }

            // عرض النتيجة في label الفيديو
            if (gameResultLabel != null) {
                gameResultLabel.setText(displayMessage);
            }

            if (showWinnerVideo)
                playVideo("/videos/winner.mp4");
            else
                playVideo("/videos/loser.mp4");
        } else {
            showRematchAlert();
        }

        // الأسماء تبقى ثابتة - مش بنغيرها!
        // opponentNameLabel بتبقى كما هي

        // إيقاف وإخفاء زرار Record بعد انتهاء اللعبة
        if (isRecording) {
            stopRecording();
        }
        if (recordBtn != null) {
            recordBtn.setVisible(false);
            recordBtn.setManaged(false);
        }
    }

    private void showRematchAlert() {
        if (gameFinishMessage == null)
            return;

        String icon = "🤝";
        if (gameFinishMessage.contains("Wins"))
            icon = "🏆";

        alertHandler.showConfirmation(gameFinishMessage.toUpperCase(), "Want to play again?",
                new CustomAlertHandler.ConfirmationCallback() {
                    @Override
                    public void onYes() {
                        if (GameSession.isOnline)
                            sendOnlinePlayAgainRequest();
                        else
                            resetBoard();
                    }

                    @Override
                    public void onNo() {
                        handleBack(null);
                    }
                });
        alertIcon.setText(icon);
    }

    private void showResultOverlay(String icon, String title) {
        alertHandler.showSuccess(title, "");
        alertIcon.setText(icon);
    }

    @FXML
    private void handlePlayAgainFromOverlay(ActionEvent event) {
        if (GameSession.isOnline) {
            if (alertTitle.getText().contains("WANTS A REMATCH")) {
                acceptOnlineRematch();
            } else {
                sendOnlinePlayAgainRequest();
            }
        } else {
            alertHandler.hide();
            handlePlayAgain(event);
        }
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

    private void resetBoard() {
        for (javafx.scene.Node node : gameGrid.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.setText("");
                btn.getStyleClass().removeAll("cell-button-x", "cell-button-o", "winning-button");
                if (!btn.getStyleClass().contains("cell-button-empty"))
                    btn.getStyleClass().add("cell-button-empty");
            }
        }
        recordedMoves.clear();
        gameOver = false;
        gameFinishMessage = null;
        isXTurn = true;
        gameStarted = false; // إعادة تعيين حالة بداية اللعبة

        // إيقاف وإخفاء التايمر
        stopGameTimer();
        if (timerLabel != null) {
            timerLabel.setVisible(false);
        }

        // إعادة إظهار زرار Record للعبة الجديدة (في وضع Online فقط)
        if (GameSession.isOnline && recordBtn != null) {
            recordBtn.setVisible(true);
            recordBtn.setManaged(true);
            recordBtn.setText("Record");
            recordBtn.setStyle("-fx-background-color: #e74c3c;");

            // إعادة ضبط أسماء اللاعبين للشكل الثابت (مش Turn)
            String myName = AuthManager.getInstance().getCurrentUsername();
            if ("X".equals(GameSession.playerSymbol)) {
                playerNameLabel.setText(playerX + " (X)");
                opponentNameLabel.setText(playerO + " (O)");
            } else {
                playerNameLabel.setText(playerO + " (O)");
                opponentNameLabel.setText(playerX + " (X)");
            }
        }

        opponentNameLabel.setStyle("");
        // الأسماء ثابتة - مش محتاجين updateTurnLabel
        updateBoardHoverState();
    }

    private void updateBoardHoverState() {
        if (gameGrid == null)
            return;

        // في حالة الـ replay أو اللعبة منتهية، نمنع الـ hover
        if (gameOver || GameSession.isReplay) {
            if (!gameGrid.getStyleClass().contains("not-my-turn"))
                gameGrid.getStyleClass().add("not-my-turn");
            return;
        }

        if (GameSession.isOnline) {
            String mySymbol = GameSession.playerSymbol;
            if (mySymbol == null)
                return;
            boolean isMyTurn = (isXTurn && "X".equals(mySymbol)) || (!isXTurn && "O".equals(mySymbol));
            if (isMyTurn) {
                gameGrid.getStyleClass().remove("not-my-turn");
            } else {
                if (!gameGrid.getStyleClass().contains("not-my-turn"))
                    gameGrid.getStyleClass().add("not-my-turn");
            }
        } else if (GameSession.vsComputer) {
            gameGrid.getStyleClass().remove("not-my-turn");
        } else {
            gameGrid.getStyleClass().remove("not-my-turn");
        }
    }

    private void sendOnlinePlayAgainRequest() {
        RequestData req = new RequestData();
        req.key = RequestType.PLAY_AGAIN;
        req.username = AuthManager.getInstance().getCurrentUsername();
        req.targetUsername = GameSession.opponentName;
        try {
            ServerConnection.getInstance().sendRequest(req);
            alertHandler.showSuccess("SENT", "Waiting for opponent...");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void acceptOnlineRematch() {
        RequestData req = new RequestData();
        req.key = RequestType.ACCEPT_INVITE;
        req.username = AuthManager.getInstance().getCurrentUsername();
        req.targetUsername = GameSession.opponentName;
        try {
            ServerConnection.getInstance().sendRequest(req);
            alertHandler.showSuccess("SENT", "Waiting for opponent...");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void rejectOnlineRematch() {
        RequestData req = new RequestData();
        req.key = RequestType.REJECT_INVITE;
        req.username = AuthManager.getInstance().getCurrentUsername();
        req.targetUsername = GameSession.opponentName;
        try {
            ServerConnection.getInstance().sendRequest(req);
            alertHandler.hide();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void updateTurnLabel() {
        // في الـ replay mode، الأسماء ثابتة ومابتتغيرش
        if (GameSession.isReplay) {
            return;
        }

        if (GameSession.isOnline) {
            String myName = AuthManager.getInstance().getCurrentUsername();
            if (isXTurn) {
                opponentNameLabel.setText(playerX + "'s Turn (X)");
            } else {
                opponentNameLabel.setText(playerO + "'s Turn (O)");
            }
        }
    }

    @FXML
    private void handlePlayAgain(ActionEvent event) {
        if (GameSession.isHistoryReplay) {
            alertHandler.hide();
            resetBoard();
            GameSession.isReplay = true;
            loadReplay(GameSession.replayFilePath);
        } else if (GameSession.isOnline) {
            if (gameOver) {
                sendOnlinePlayAgainRequest();
            } else {
                alertHandler.showError("WAIT!", "Finish the current game first.");
            }
        } else {
            resetBoard();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            if (GameSession.isOnline) {
                // حفظ الـ recording قبل الانسحاب إذا كان مفعل
                if (isRecording && !recordedMoves.isEmpty()) {
                    gameFinishMessage = "You Left. Opponent Wins!";
                    stopRecording();
                }

                updateUserStatus(AuthManager.getInstance().getCurrentUsername(), "online");

                RequestData req = new RequestData();
                req.key = RequestType.WITHDRAW;
                req.username = AuthManager.getInstance().getCurrentUsername();
                req.targetUsername = GameSession.opponentName;
                ServerConnection.getInstance().sendRequest(req);
                ServerConnection.getInstance().setGameMoveListener(null);

                GameSession.isOnline = false;
                NavigationManager.switchSceneUsingNode(gameGrid,
                        AppConstants.PATH_GAME_LOBBY);
            } else if (GameSession.vsComputer) {
                NavigationManager.switchSceneUsingNode(gameGrid,
                        "/com/mycompany/finalprojectclient/vsComputer.fxml");
            } else if (GameSession.isReplay) {
                GameSession.isReplay = false;
                NavigationManager.switchSceneUsingNode(gameGrid,
                        AppConstants.PATH_GAME_HISTORY);
            } else {
                NavigationManager.switchSceneUsingNode(gameGrid,
                        AppConstants.PATH_ON_OFF);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateScoreLabels() {
        if (GameSession.isOnline) {
            System.out.println("=== updateScoreLabels called ===");
            // Online game: نطلب الـ scores من السيرفر
            if (scoreX != null && scoreO != null) {
                // نعرض 0 كـ default (المستخدمين الجدد بيبدأوا بـ 0)
                scoreX.setText("Score: 0");
                scoreO.setText("Score: 0");

                // نطلب الـ scores الحقيقية من السيرفر (هتيجي عن طريق ScoreListener)
                // leftPlayer على الشمال (scoreX)، rightPlayer على اليمين (scoreO)
                if (leftPlayer != null && !leftPlayer.isEmpty()) {
                    System.out.println("Requesting score for left player (scoreX): " + leftPlayer);
                    ServerConnection.getInstance().requestUserScore(leftPlayer);
                } else {
                    System.out.println("ERROR: leftPlayer is null or empty!");
                }

                if (rightPlayer != null && !rightPlayer.isEmpty()) {
                    System.out.println("Requesting score for right player (scoreO): " + rightPlayer);
                    ServerConnection.getInstance().requestUserScore(rightPlayer);
                } else {
                    System.out.println("ERROR: rightPlayer is null or empty!");
                }
            }
        } else {
            // Local game: نعرض match wins
            scoreX.setText(playerX + " (X): " + countX);
            scoreO.setText(playerO + " (O): " + countO);
        }
    }

    private void playVideo(String videoPath) {
        if (videoMediaView == null || videoOverlay == null) {
            System.err.println("Video components not found. Skipping video.");
            return;
        }
        try {
            System.out.println("Attempting to play video: " + videoPath);
            URL videoUrl = MainApp.class.getResource(videoPath);
            if (videoUrl == null) {
                System.err.println("Video file not found: " + videoPath);
                return;
            }
            System.out.println("Video URL: " + videoUrl.toExternalForm());

            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            Media media = new Media(videoUrl.toExternalForm());

            media.setOnError(() -> {
                System.err.println("Media Load Error: " + media.getError().getMessage());
                handleCloseVideo(null);
            });

            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnError(() -> {
                System.err.println("MediaPlayer Playback Error: " + mediaPlayer.getError().getMessage());
                handleCloseVideo(null);
            });

            videoMediaView.setMediaPlayer(mediaPlayer);

            videoOverlay.setVisible(true);
            videoOverlay.setManaged(true);
            videoOverlay.setOpacity(0);

            FadeTransition ft = new FadeTransition(Duration.millis(500), videoOverlay);
            ft.setToValue(1.0);
            ft.play();

            mediaPlayer.play();

            mediaPlayer.setOnEndOfMedia(() -> {
            });

        } catch (Exception e) {
            System.err.println("Exception in playVideo: " + e.getMessage());
            e.printStackTrace();
            handleCloseVideo(null);
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

    @FXML
    private void handleCloseButtonHover(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof javafx.scene.control.Button) {
            javafx.scene.control.Button btn = (javafx.scene.control.Button) event.getSource();
            btn.setStyle("-fx-background-color: linear-gradient(to bottom, #ff85b3, #d95a8f); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; " +
                    "-fx-padding: 12 50; -fx-background-radius: 25; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(255, 107, 157, 0.9), 15, 0.6, 0, 4);");
        }
    }

    @FXML
    private void handleCloseButtonExit(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof javafx.scene.control.Button) {
            javafx.scene.control.Button btn = (javafx.scene.control.Button) event.getSource();
            btn.setStyle("-fx-background-color: linear-gradient(to bottom, #ff6b9d, #c94277); " +
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; " +
                    "-fx-padding: 12 50; -fx-background-radius: 25; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(255, 107, 157, 0.6), 10, 0.5, 0, 3);");
        }
    }

    private void updateUserStatus(String username, String status) {
        try {
            RequestData request = new RequestData();
            request.key = RequestType.UPDATE_STATUS;
            request.username = username;
            request.status = status;
            ServerConnection.getInstance().sendRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void startGameTimer() {
        if (timerLabel == null || !GameSession.isOnline)
            return;

        elapsedSeconds = 0;
        timerLabel.setVisible(true);
        timerLabel.setText("⏱ 00:00");

        gameTimer = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(1), e -> {
                    elapsedSeconds++;
                    int minutes = elapsedSeconds / 60;
                    int seconds = elapsedSeconds % 60;
                    timerLabel.setText(String.format("⏱ %02d:%02d", minutes, seconds));
                }));
        gameTimer.setCycleCount(javafx.animation.Animation.INDEFINITE);
        gameTimer.play();
    }

    private void stopGameTimer() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
    }

    /**
     * إرسال نتيجة اللعبة للسيرفر لتحديث الـ scores
     * يحلل رسالة نهاية اللعبة ويحدد الفائز والخاسر والنتيجة
     */
    private void sendGameEndToServer(String gameMessage) {
        if (!GameSession.isOnline) {
            return; // مش online game
        }

        String currentUser = AuthManager.getInstance().getCurrentUsername();
        String opponent = GameSession.opponentName;

        if (currentUser == null || opponent == null) {
            System.err.println("Cannot send game end: missing player names");
            return;
        }

        System.out.println("Game ended with message: " + gameMessage);
        System.out.println("Current user: " + currentUser + ", Symbol: " + GameSession.playerSymbol);
        System.out.println("Opponent: " + opponent);

        // تحديد نوع النتيجة بناءً على الرسالة
        if (gameMessage.toLowerCase().contains("draw")) {
            // تعادل
            ServerConnection.getInstance().sendGameEnd(currentUser, opponent, "DRAW");

        } else if (gameMessage.toLowerCase().contains("withdraw") ||
                gameMessage.toLowerCase().contains("left")) {
            // انسحاب - اللي انسحب هو اللي خسر
            if (gameMessage.toLowerCase().contains("you")) {
                // You withdrew
                ServerConnection.getInstance().sendGameEnd(opponent, currentUser, "WITHDRAW");
            } else {
                // Opponent withdrew
                ServerConnection.getInstance().sendGameEnd(currentUser, opponent, "WITHDRAW");
            }

        } else if (gameMessage.toLowerCase().contains("win")) {
            // فوز - نحدد الفائز بناءً على الـ symbol
            String winningSymbol = null;
            if (gameMessage.contains("X Wins")) {
                winningSymbol = "X";
            } else if (gameMessage.contains("O Wins")) {
                winningSymbol = "O";
            }

            if (winningSymbol != null) {
                // نشوف مين الفائز بناءً على الـ symbol
                if (winningSymbol.equals(GameSession.playerSymbol)) {
                    // أنا الفائز - أنا بس اللي هبلغ السيرفر
                    ServerConnection.getInstance().sendGameEnd(currentUser, opponent, "WIN");
                }
                // الخاسر مايبلغش السيرفر عشان ميبقاش duplicate
            }
        }
    }

    /**
     * تحديث عرض الـ score في الـ UI
     * يتم استدعاؤها من ScoreListener لما يجي SCORE_UPDATE من السيرفر
     */
    private void updatePlayerScore(String username, int newScore) {
        System.out.println("=== updatePlayerScore called ===");
        System.out.println("Username: " + username + ", newScore: " + newScore);
        System.out.println("leftPlayer: " + leftPlayer + ", rightPlayer: " + rightPlayer);

        if (scoreX == null || scoreO == null) {
            System.out.println("ERROR: scoreX or scoreO is null!");
            return; // Labels مش initialized بعد
        }

        // scoreX دايماً على الشمال جنب playerNameLabel
        // scoreO دايماً على اليمين جنب opponentNameLabel
        // نحدث بناءً على الموقع (left/right) مش الـ symbol (X/O)
        if (username.equalsIgnoreCase(leftPlayer)) {
            System.out.println("Updating scoreX (left) to: " + newScore);
            scoreX.setText("Score: " + newScore); // على الشمال
        } else if (username.equalsIgnoreCase(rightPlayer)) {
            System.out.println("Updating scoreO (right) to: " + newScore);
            scoreO.setText("Score: " + newScore); // على اليمين
        } else {
            System.out.println("WARNING: username '" + username + "' doesn't match leftPlayer or rightPlayer!");
        }
    }
}
