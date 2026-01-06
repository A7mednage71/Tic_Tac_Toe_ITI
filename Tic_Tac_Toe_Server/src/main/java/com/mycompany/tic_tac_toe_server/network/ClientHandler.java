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

    public ClientHandler(Socket socket) {
        this.socket = socket;
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

    private void logout() {
        if (username != null) {
            ServerThread.onlineUsers.remove(this);
            UserDAO.getInstance().updateUserStatus(username, "Disactive");
            System.out.println("User " + username + " is now disactive in DB.");

            ServerThread.broadcastUserListUpdate();
        }
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
            e.printStackTrace();
        }
    }
}