package com.mycompany.finalprojectclient.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class NavigationManager {

    public static void switchScene(ActionEvent event, String fxmlPath) {
        switchSceneUsingNode((Node) event.getSource(), fxmlPath);
    }

    public static void switchSceneUsingNode(Node node, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) node.getScene().getWindow();
            Scene scene = stage.getScene();

            scene.setRoot(root);

        } catch (IOException e) {
            System.err.println("Navigation Error: Could not load " + fxmlPath);
            e.printStackTrace();
        }
    }
}
