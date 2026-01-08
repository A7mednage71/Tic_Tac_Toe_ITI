package com.mycompany.finalprojectclient.models;

import java.util.List;

public class GameRecord {

    private String gameId;
    private String playerX;
    private String playerO;
    private String date;
    private String result;
    private List<String> moves;

    private transient String fileName;
    private transient String move1;
    private transient String move2;

    public GameRecord() {
    }

    public GameRecord(String gameId, String playerX, String playerO, String result,
            String date, List<String> moves) {
        this.gameId = gameId;
        this.playerX = playerX;
        this.playerO = playerO;
        this.result = result;
        this.date = date;
        this.moves = moves;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayerX() {
        return playerX;
    }

    public void setPlayerX(String playerX) {
        this.playerX = playerX;
    }

    public String getPlayerO() {
        return playerO;
    }

    public void setPlayerO(String playerO) {
        this.playerO = playerO;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<String> getMoves() {
        return moves;
    }

    public void setMoves(List<String> moves) {
        this.moves = moves;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPlayer1() {
        return playerX;
    }

    public String getPlayer2() {
        return playerO;
    }

    public String getWinner() {
        return result;
    }

    public String getMove1() {
        if (move1 == null && moves != null && !moves.isEmpty()) {
            String firstMove = moves.get(0);
            if (firstMove.contains(",")) {
                String[] parts = firstMove.split(",");
                if (parts.length >= 3) {
                    move1 = parts[2].trim();
                }
            } else {
                move1 = firstMove.trim();
            }
        }
        return move1 != null ? move1 : "-";
    }

    public String getMove2() {
        if (move2 == null && moves != null && moves.size() > 1) {
            String secondMove = moves.get(1);
            if (secondMove.contains(",")) {
                String[] parts = secondMove.split(",");
                if (parts.length >= 3) {
                    move2 = parts[2].trim();
                }
            } else {
                move2 = secondMove.trim();
            }
        }
        return move2 != null ? move2 : "-";
    }
}
