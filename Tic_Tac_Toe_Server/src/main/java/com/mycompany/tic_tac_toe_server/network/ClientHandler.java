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

    private String username;
    private String status = "active";

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            requestManager = new RequestManager(dos, this);

            while (isRunning) {
                String jsonRequest = dis.readUTF();
                System.out.println("Received JSON: " + jsonRequest);
                try {
                    RequestData request = gson.fromJson(jsonRequest, RequestData.class);
                    if (request != null && request.key != null) {
                        requestManager.processRequest(request);
                    }
                } catch (JsonSyntaxException e) {
                    System.err.println("Wrong JSON format");
                }
            }

        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            logout();
            closeConnection();
        }
    }

    public void setUsername(String username) {
        this.username = username;
        ServerThread.onlineUsers.add(this);
    }

    public String getUsername() {
        return username;
    }

    public void sendUserListUpdate() {
        try {
            if (dos != null) {
                dos.writeUTF("USER_LIST_UPDATED");
                dos.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending user list update: " + e.getMessage());
        }
    }

    public void sendInvite(String fromUsername) {
        try {
            if (dos != null) {
                dos.writeUTF("INVITE_FROM:" + fromUsername);
                dos.flush();
                System.out.println("Sent invite notification to " + username + " from " + fromUsername);
            }
        } catch (IOException e) {
            System.err.println("Error sending invite: " + e.getMessage());
        }
    }

    public void sendInviteAccepted(String acceptingUsername) {
        try {
            if (dos != null) {
                dos.writeUTF("INVITE_ACCEPTED:" + acceptingUsername);
                dos.flush();
                System.out.println("Sent invite accepted notification to " + username);
            }
        } catch (IOException e) {
            System.err.println("Error sending invite accepted: " + e.getMessage());
        }
    }

    public void sendInviteRejected(String rejectingUsername) {
        try {
            if (dos != null) {
                dos.writeUTF("INVITE_REJECTED:" + rejectingUsername);
                dos.flush();
                System.out.println("Sent invite rejected notification to " + username);
            }
        } catch (IOException e) {
            System.err.println("Error sending invite rejected: " + e.getMessage());
        }
    }

    public void sendWithdrawNotification(String fromUsername) {
        try {
            if (dos != null) {
                dos.writeUTF("OPPONENT_WITHDREW:" + fromUsername);
                dos.flush();
                System.out.println("Sent withdraw notification to " + username);
            }
        } catch (IOException e) {
            System.err.println("Error sending withdraw: " + e.getMessage());
        }
    }

    private void logout() {
        if (username != null) {
            ServerThread.onlineUsers.remove(this);
            UserDAO.getInstance().updateUserStatus(username, "Disactive");
            System.out.println("User " + username + " is now disactive in DB.");

            ServerThread.broadcastUserListUpdate();
        }
    }

    public void closeConnection() {
        isRunning = false;
        try {
            if (dis != null)
                dis.close();
            if (dos != null)
                dos.close();
            if (socket != null && !socket.isClosed())
                socket.close();
            UserDAO.getInstance().updateUserStatus(username, "disactive");
            System.out.println("Connection closed for user: " + (username != null ? username : "Unknown"));
        } catch (IOException e) {
            System.out.println("Error in close Connection for user: " + (username != null ? username : "Unknown"));
        }
    }
}