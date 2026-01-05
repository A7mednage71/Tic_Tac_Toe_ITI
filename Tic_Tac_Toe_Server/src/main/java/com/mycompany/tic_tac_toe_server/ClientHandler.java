package com.mycompany.tic_tac_toe_server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private boolean isRunning = true;
    private final Gson gson = new Gson();
    private RequestManager requestManager;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            //handel ِAll Requests are here
            requestManager = new RequestManager(dos, this);

            while (isRunning) {
                String jsonRequest = dis.readUTF();
                System.out.println("Received JSON: " + jsonRequest);
                try {
                    RequestData request = gson.fromJson(jsonRequest, RequestData.class);
                    if (request.key != null) {
                        requestManager.processRequest(request);
                    }
                } catch (JsonSyntaxException e) {
                    System.err.println("Wrong JSON format");
                }
            }

        } catch (IOException e) {
            System.err.println("Client disconnected: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    public void closeConnection() {
        isRunning = false;
        try {
            if (dis != null) {
                dis.close();
            }
            if (dos != null) {
                dos.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
