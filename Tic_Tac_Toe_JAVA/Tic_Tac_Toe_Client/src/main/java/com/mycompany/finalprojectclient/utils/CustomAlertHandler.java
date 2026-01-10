package com.mycompany.finalprojectclient.utils;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CustomAlertHandler {

    private final StackPane overlay;
    private final VBox alertBox;
    private final Label titleLabel;
    private final Label messageLabel;
    private final Label iconLabel;

    public CustomAlertHandler(StackPane overlay, VBox alertBox, Label titleLabel, Label messageLabel, Label iconLabel) {
        this.overlay = overlay;
        this.alertBox = alertBox;
        this.titleLabel = titleLabel;
        this.messageLabel = messageLabel;
        this.iconLabel = iconLabel;
    }

    public void showSuccess(String title, String message) {
        setupAlert(title, message, "alert-box-success", "#2ecc71", "✅");
        UIAnimations.popIn(alertBox);
    }

    public void showError(String title, String message) {
        setupAlert(title, message, "alert-box-error", "#ff4757", "❌");
        UIAnimations.popIn(alertBox);
        UIAnimations.shake(alertBox);
    }

    public void showConfirmation(String title, String message, ConfirmationCallback callback) {
        titleLabel.setText(title);
        messageLabel.setText(message);
        if (iconLabel != null)
            iconLabel.setText("");

        alertBox.getStyleClass().removeAll("alert-box-error", "alert-box-success");
        alertBox.getStyleClass().add("alert-box-success");
        titleLabel.setStyle("-fx-text-fill: #88a050; -fx-font-size: 20px; -fx-font-weight: bold;");
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        alertBox.getChildren().removeIf(node -> node instanceof HBox);

        Button yesButton = new Button("ACCEPT");
        yesButton.setStyle(
                "-fx-background-color: #6a8c3d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 25; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 13; -fx-border-color: #88a050; -fx-border-radius: 10; -fx-border-width: 2;");
        yesButton.setOnAction(e -> {
            hide();
            if (callback != null)
                callback.onYes();
        });

        Button noButton = new Button("REJECT");
        noButton.setStyle(
                "-fx-background-color: #8B4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 25; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 13; -fx-border-color: #B85C5C; -fx-border-radius: 10; -fx-border-width: 2;");
        noButton.setOnAction(e -> {
            hide();
            if (callback != null)
                callback.onNo();
        });

        HBox buttonBox = new HBox(15, yesButton, noButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 10 0 0 0;");

        alertBox.getChildren().add(buttonBox);

        overlay.setManaged(true);
        overlay.setVisible(true);
        overlay.toFront();
        UIAnimations.popIn(alertBox);
        UIAnimations.bounce(alertBox);
    }

    public void showLoading(String title, String message) {
        showLoading(title, message, null);
    }

    public void showLoading(String title, String message, CancelCallback callback) {
        setupAlert(title, message, "alert-box-success", "#f39c12", "");
        alertBox.getChildren().removeIf(node -> node instanceof HBox);

        if (callback != null) {
            Button cancelButton = new Button("CANCEL");
            cancelButton.setStyle(
                    "-fx-background-color: #8B4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 30; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 13; -fx-border-color: #B85C5C; -fx-border-radius: 10; -fx-border-width: 2;");
            cancelButton.setOnAction(e -> {
                hide();
                if (callback != null)
                    callback.onCancel();
            });

            HBox buttonBox = new HBox(cancelButton);
            buttonBox.setAlignment(Pos.CENTER);
            buttonBox.setStyle("-fx-padding: 10 0 0 0;");
            alertBox.getChildren().add(buttonBox);
        }

        UIAnimations.popIn(alertBox);
    }

    public void hide() {
        UIAnimations.popOut(alertBox, () -> {
            overlay.setVisible(false);
            overlay.setManaged(false);
            alertBox.getChildren().removeIf(node -> node instanceof HBox);
            alertBox.setScaleX(1);
            alertBox.setScaleY(1);
        });
    }

    private void setupAlert(String title, String message, String cssClass, String colorHex, String icon) {
        titleLabel.setText(title);
        messageLabel.setText(message);
        if (iconLabel != null)
            iconLabel.setText(icon);

        alertBox.getStyleClass().removeAll("alert-box-error", "alert-box-success");
        alertBox.getStyleClass().add(cssClass);
        titleLabel.setStyle("-fx-text-fill: " + colorHex + "; -fx-font-size: 22px; -fx-font-weight: bold;");
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px;");

        alertBox.getChildren().removeIf(node -> node instanceof HBox);

        Button closeButton = new Button("OK");
        closeButton.setStyle(
                "-fx-background-color: #6a8c3d; -fx-text-fill: white; -fx-padding: 10 35; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-weight: bold; -fx-font-size: 14; -fx-border-color: #88a050; -fx-border-radius: 10; -fx-border-width: 2;");
        closeButton.setOnAction(e -> hide());

        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20 0 0 0;");
        alertBox.getChildren().add(buttonBox);

        overlay.setManaged(true);
        overlay.setVisible(true);
        overlay.toFront();
    }

    public interface ConfirmationCallback {
        void onYes();

        void onNo();
    }

    public interface CancelCallback {
        void onCancel();
    }
}
