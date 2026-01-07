package com.mycompany.tic_tac_toe_server.utils;

import com.mycompany.tic_tac_toe_server.controllers.CustomAlertController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.io.IOException;

public class CustomAlert {

    public static void show(Window owner, AlertType type, String title, String message) {
        try {
            FXMLLoader loader = new FXMLLoader(CustomAlert.class.getResource("/com/mycompany/tic_tac_toe_server/custom_alert.fxml"));
            Scene scene = new Scene(loader.load());
            scene.setFill(Color.TRANSPARENT);

            CustomAlertController controller = loader.getController();

            Stage stage = new Stage();
            stage.initStyle(StageStyle.TRANSPARENT);

            if (owner != null) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            } else {
                stage.initModality(Modality.APPLICATION_MODAL);
            }

            stage.setScene(scene);

            controller.setDialogStage(stage);
            controller.setAlertData(type, title, message);

            if (owner != null) {
                stage.setOnShown(e -> {
                    double centerX = owner.getX() + (owner.getWidth() / 2) - (stage.getWidth() / 2);
                    double centerY = owner.getY() + (owner.getHeight() / 2) - (stage.getHeight() / 2);
                    stage.setX(centerX);
                    stage.setY(centerY);
                });
            }

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
