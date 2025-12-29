package com.mycompany.finalprojectclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GameController {

    @FXML 
    private Label lblScore;

    private int score = 0;
    private String email = "test@test.com"; 

    public void addScore() {
        try {
            score += 10;

            Socket socket = new Socket("localhost", 5000);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            dos.writeUTF("UPDATE_SCORE");
            dos.writeUTF(email);
            dos.writeInt(score);

            dis.readUTF();
            lblScore.setText("Score: " + score);

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
