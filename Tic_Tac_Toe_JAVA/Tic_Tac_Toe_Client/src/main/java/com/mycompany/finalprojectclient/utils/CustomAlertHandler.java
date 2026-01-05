package com.mycompany.finalprojectclient.utils;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CustomAlertHandler {

    private final StackPane overlay;
    private final VBox alertBox;
    private final Label titleLabel;
    private final Label messageLabel;

    public CustomAlertHandler(StackPane overlay, VBox alertBox, Label titleLabel, Label messageLabel) {
        this.overlay = overlay;
        this.alertBox = alertBox;
        this.titleLabel = titleLabel;
        this.messageLabel = messageLabel;
    }

    public void showSuccess(String title, String message) {
        setupAlert(title, message, "alert-box-success", "#2ecc71");
        UIAnimations.popIn(alertBox);
    }

    public void showError(String title, String message) {
        setupAlert(title, message, "alert-box-error", "#ff4757");
        UIAnimations.popIn(alertBox);
        UIAnimations.shake(alertBox);
    }

    public void hide() {
        UIAnimations.popOut(alertBox, () -> {
            overlay.setVisible(false);
            overlay.setManaged(false);
        });
    }

    private void setupAlert(String title, String message, String cssClass, String colorHex) {
        titleLabel.setText(title);
        messageLabel.setText(message);

        alertBox.getStyleClass().removeAll("alert-box-error", "alert-box-success");
        alertBox.getStyleClass().add(cssClass);
        titleLabel.setStyle("-fx-text-fill: " + colorHex + ";");

        overlay.setManaged(true);
        overlay.setVisible(true);
        overlay.toFront();
    }
}
