package com.mycompany.tic_tac_toe_server.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CustomAlertController {

    @FXML
    private Label iconLabel;
    @FXML
    private Label titleLabel;
    @FXML
    private Label messageLabel;

    private Stage dialogStage;

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setAlertData(AlertType type, String title, String message) {
        titleLabel.setText(title);
        messageLabel.setText(message);

        switch (type) {
            case ERROR -> {
                iconLabel.setText("❌");
                titleLabel.getStyleClass().add("alert-title-error");
            }
            case INFORMATION -> {
                iconLabel.setText("✅");
                titleLabel.getStyleClass().add("alert-title-success");
            }
            case WARNING -> {
                iconLabel.setText("⚠️");
                titleLabel.getStyleClass().add("alert-title-warning");
            }
            default -> {
                iconLabel.setText("ℹ️");
                titleLabel.getStyleClass().add("alert-title-info");
            }
        }
    }

    @FXML
    private void closeAlert() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}
