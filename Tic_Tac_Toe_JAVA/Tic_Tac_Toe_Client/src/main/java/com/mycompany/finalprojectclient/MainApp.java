package com.mycompany.finalprojectclient;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/app_icon.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("Icon not found, continuing without it.");
        }
        Parent root = FXMLLoader.load(
                getClass().getResource("/com/mycompany/finalprojectclient/Home.fxml"));
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setTitle("Tic Tac Toe");
        stage.setOnCloseRequest(e -> {
            javafx.application.Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
