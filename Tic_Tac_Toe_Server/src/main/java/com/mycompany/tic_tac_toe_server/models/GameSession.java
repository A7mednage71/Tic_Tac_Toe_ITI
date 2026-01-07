/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tic_tac_toe_server.models;

import com.mycompany.tic_tac_toe_server.network.ClientHandler;

public class GameSession {
    private ClientHandler playerX;
    private ClientHandler playerO;
    private char[][] board = new char[3][3];
    private boolean xTurn = true;

    public GameSession(ClientHandler p1, ClientHandler p2) {
        this.playerX = p1;
        this.playerO = p2;
    }

    // Methods to handle move logic and broadcasting moves ONLY to these 2 players
    public void broadcastMove(int row, int col, String type) {
        playerX.sendMessage("MOVE|" + row + "|" + col + "|" + type);
        playerO.sendMessage("MOVE|" + row + "|" + col + "|" + type);
    }
}
