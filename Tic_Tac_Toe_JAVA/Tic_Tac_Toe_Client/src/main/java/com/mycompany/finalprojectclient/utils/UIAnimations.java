package com.mycompany.finalprojectclient.utils;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class UIAnimations {

    public static void shake(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setByX(10);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }

    public static void popIn(Node node) {
        ScaleTransition st = new ScaleTransition(Duration.millis(300), node);
        st.setFromX(0);
        st.setFromY(0);
        st.setToX(1);
        st.setToY(1);
        st.play();
    }

    public static void popOut(Node node, Runnable onFinished) {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), node);
        st.setToX(0);
        st.setToY(0);
        st.setOnFinished(e -> {
            if (onFinished != null) {
                onFinished.run();
            }
        });
        st.play();
    }
}
