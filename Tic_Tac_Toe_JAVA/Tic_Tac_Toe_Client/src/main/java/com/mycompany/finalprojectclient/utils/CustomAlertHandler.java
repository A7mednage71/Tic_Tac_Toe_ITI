package com.mycompany.finalprojectclient.utils;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

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
        if (iconLabel != null) iconLabel.setText("🎮");

        alertBox.getStyleClass().removeAll("alert-box-error", "alert-box-success");
        alertBox.getStyleClass().add("alert-box-success");
        titleLabel.setStyle("-fx-text-fill: #f39c12;");

        // Remove existing buttons if any
        alertBox.getChildren().removeIf(node -> node instanceof HBox);

        // Create Yes/No buttons with better styling
        Button yesButton = new Button("ACCEPT");
        yesButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 35; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 14;");
        yesButton.setOnAction(e -> {
            hide();
            if (callback != null) callback.onYes();
        });

        Button noButton = new Button("REJECT");
        noButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 35; -fx-background-radius: 10; -fx-cursor: hand; -fx-font-size: 14;");
        noButton.setOnAction(e -> {
            hide();
            if (callback != null) callback.onNo();
        });

        HBox buttonBox = new HBox(20, yesButton, noButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 20 0 0 0;");

        alertBox.getChildren().add(buttonBox);

        overlay.setManaged(true);
        overlay.setVisible(true);
        overlay.toFront();
        UIAnimations.popIn(alertBox);
    }

    public void showLoading(String title, String message) {
        setupAlert(title, message, "alert-box-success", "#f39c12", "⏳");
        // No buttons for loading, just the message
        alertBox.getChildren().removeIf(node -> node instanceof HBox);
        UIAnimations.popIn(alertBox);
    }

    public void hide() {
        UIAnimations.popOut(alertBox, () -> {
            overlay.setVisible(false);
            overlay.setManaged(false);
            // Reset state and remove buttons
            alertBox.getChildren().removeIf(node -> node instanceof HBox);
            alertBox.setScaleX(1);
            alertBox.setScaleY(1);
        });
    }

    private void setupAlert(String title, String message, String cssClass, String colorHex, String icon) {
        titleLabel.setText(title);
        messageLabel.setText(message);
        if (iconLabel != null) iconLabel.setText(icon);

        alertBox.getStyleClass().removeAll("alert-box-error", "alert-box-success");
        alertBox.getStyleClass().add(cssClass);
        titleLabel.setStyle("-fx-text-fill: " + colorHex + ";");

        // Remove buttons if any
        alertBox.getChildren().removeIf(node -> node instanceof HBox);

        // Add a "Close" button for simple alerts
        Button closeButton = new Button("OK");
        closeButton.setStyle("-fx-background-color: #4A443F; -fx-text-fill: white; -fx-padding: 8 30; -fx-background-radius: 5; -fx-cursor: hand; -fx-font-weight: bold;");
        closeButton.setOnAction(e -> hide());
        
        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 15 0 0 0;");
        alertBox.getChildren().add(buttonBox);

        overlay.setManaged(true);
        overlay.setVisible(true);
        overlay.toFront();
    }

    public interface ConfirmationCallback {
        void onYes();
        void onNo();
    }
}
