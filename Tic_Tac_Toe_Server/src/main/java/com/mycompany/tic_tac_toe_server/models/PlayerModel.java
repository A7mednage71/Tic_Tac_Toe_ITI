package com.mycompany.tic_tac_toe_server.models;

public class PlayerModel {

    private String username;
    private int score;
    private String status;

    public PlayerModel(String username, int score, String status) {
        this.username = username;
        this.score = score;
        this.status = status;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
