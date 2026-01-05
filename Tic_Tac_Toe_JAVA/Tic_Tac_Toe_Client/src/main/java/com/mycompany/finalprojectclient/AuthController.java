package com.mycompany.finalprojectclient;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition; // أنيميشن التكبير
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AuthController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private StackPane customAlertOverlay;
    @FXML private VBox alertBox;
    @FXML private Label alertTitle;
    @FXML private Label alertMessage;
    @FXML
    private StackPane rootStackPane;
    @FXML
    private Button loginButton;

    private void switchScene(String fxml, ActionEvent e) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/finalprojectclient/" + fxml));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    // الميثود دي بقت ذكية: بتغير الألوان والأنيميشن حسب الحالة
    private void showCustomAlert(String title, String message, boolean isSuccess) {
        alertTitle.setText(title);
        alertMessage.setText(message);
        
        // مسح الستايلات القديمة وإضافة الجديد بناءً على الحالة
        alertBox.getStyleClass().removeAll("alert-box-error", "alert-box-success");
        
        if (isSuccess) {
            alertBox.getStyleClass().add("alert-box-success");
            alertTitle.setStyle("-fx-text-fill: #2ecc71;"); // أخضر صايع
        } else {
            alertBox.getStyleClass().add("alert-box-error");
            alertTitle.setStyle("-fx-text-fill: #ff4757;"); // أحمر ناري
        }

        customAlertOverlay.setManaged(true);
        customAlertOverlay.setVisible(true);
        customAlertOverlay.toFront(); 

        // أنيميشن الـ Pop-up (بيكبر من 0 لـ 1)
        ScaleTransition st = new ScaleTransition(Duration.millis(300), alertBox);
        st.setFromX(0);
        st.setFromY(0);
        st.setToX(1);
        st.setToY(1);
        st.play();

        // لو فشل، خليه يتهز (Shake)
        if (!isSuccess) {
            shakeAnimation(alertBox);
        }
    }

    private void shakeAnimation(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    @FXML
    private void closeCustomAlert() {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), alertBox);
        st.setToX(0);
        st.setToY(0);
        st.setOnFinished(e -> {
            customAlertOverlay.setVisible(false);
            customAlertOverlay.setManaged(false);
        });
        st.play();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showCustomAlert("Missing Info", "Please enter your credentials!", false);
            return;
        }

        try {
            String response = ServerConnection.getInstance().sendRequest("LOGIN", new String[] { username, password });

            if (response.equals("LOGIN_SUCCESS")) {
                // رسالة نجاح صايعة باسم المستخدم
                showCustomAlert("WELCOME HERO!", "Hello " + username.toUpperCase() + ", redirecting to lobby...", true);
                
                // تأخير ثانيتين عشان يشوف العظمة دي
                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(e -> {
                    try {
                        switchScene("TicTacToeLobby.fxml", event);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                pause.play();

            } else {
                showCustomAlert("ACCESS DENIED", "Invalid username or password.", false);
                passwordField.clear();
            }

        } catch (Exception ex) {
            showCustomAlert("SERVER ERROR", "Couldn't connect to server.", false);
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleSignUp(ActionEvent event) {
        try {
            switchScene("register.fxml", event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
        switchScene("online&offline.fxml", event);
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    }
}