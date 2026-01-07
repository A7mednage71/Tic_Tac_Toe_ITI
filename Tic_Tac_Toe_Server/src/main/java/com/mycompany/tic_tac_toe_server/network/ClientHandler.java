package com.mycompany.tic_tac_toe_server.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mycompany.tic_tac_toe_server.database.UserDAO;
import com.mycompany.tic_tac_toe_server.models.RequestData;

public class ClientHandler extends Thread {

    private final Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isRunning = true;
    private final Gson gson = new Gson();
    private RequestManager requestManager;

    // Game Logic Fields
    private ClientHandler opponent; 
    private boolean isInGame = false;
    
    private String username;
    private String status = "active";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    // for matchmaking
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
    // --------------------
    
    // --- Game Logic Methods ---

    public void setOpponent(ClientHandler opp) {
        this.opponent = opp;
        this.isInGame = (opp != null);
        this.status = (opp != null) ? "busy" : "active"; // Update status if in game
    }

    /**
     * Helper to send strings safely to this specific client
     */
    public void sendMessage(String message) {
        try {
            if (dos != null) {
                dos.writeUTF(message);
                dos.flush();
            }
        } catch (IOException e) {
            System.err.println("Failed to send message to " + username);
        }
    }

    /**
     * Processes game-specific raw strings (e.g., MOVE|row|col)
     */
    private void handleGameData(String data) {
        if (data.startsWith("MOVE|") && isInGame && opponent != null) {
            // Forward the move directly to the opponent's device
            opponent.sendMessage(data);
            System.out.println("Forwarding move from " + username + " to " + opponent.getUsername());
        } 
        // Note: INVITE logic is likely handled via JSON in your RequestManager, 
        // but if you want it here as a string, you can add it.
    }

    // --- Core Logic ---

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            requestManager = new RequestManager(dos, this);

            while (isRunning) {
                String input = dis.readUTF();
                
                // DECIDE: Is this a Game Move (String) or a System Request (JSON)?
                if (input.startsWith("MOVE|")) {
                    handleGameData(input);
                } else {
                    // Try to process as JSON for Login, Registration, User Lists, etc.
                    try {
                        RequestData request = gson.fromJson(input, RequestData.class);
                        if (request != null && request.key != null) {
                            requestManager.processRequest(request);
                        }
                    } catch (JsonSyntaxException e) {
                        // If it's not valid JSON and not a MOVE, it's an unknown protocol
                        System.err.println("Unknown protocol received from " + username + ": " + input);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Client disconnected: " + username);
        } finally {
            logout();
            closeConnection();
        }
    }

    // --- Existing UI/Status Methods (Kept for compatibility) ---

    public void setUsername(String username) {
        this.username = username;
        ServerThread.onlineUsers.add(this);
    }

    public String getUsername() { return username; }

    public void sendUserListUpdate() {
        sendMessage("USER_LIST_UPDATED");
    }

    public void sendInvite(String fromUsername) {
        sendMessage("INVITE_FROM:" + fromUsername);
    }

    public void sendInviteAccepted(String acceptingUsername) {
        sendMessage("INVITE_ACCEPTED:" + acceptingUsername);
    }

    public void sendInviteRejected(String rejectingUsername) {
        sendMessage("INVITE_REJECTED:" + rejectingUsername);
    }

    public void sendWithdrawNotification(String fromUsername) {
        sendMessage("OPPONENT_WITHDREW:" + fromUsername);
    }

    private void logout() {
        if (username != null) {
            ServerThread.onlineUsers.remove(this);
            UserDAO.getInstance().updateUserStatus(username, "Disactive");
            
            // Notify opponent if this user leaves mid-game
            if (opponent != null) {
                opponent.sendMessage("OPPONENT_DISCONNECTED");
                opponent.setOpponent(null); // Free the opponent to play again
            }

            ServerThread.broadcastUserListUpdate();
        }
    }

    public void closeConnection() {
        isRunning = false;
        try {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.out.println("Error closing connection for " + username);
        }
    }
}