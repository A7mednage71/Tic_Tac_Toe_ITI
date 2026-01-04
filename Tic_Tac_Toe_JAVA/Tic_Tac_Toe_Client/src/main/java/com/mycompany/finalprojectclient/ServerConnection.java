package com.mycompany.finalprojectclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerConnection {

    private static ServerConnection instance;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5002;

    private ServerConnection() {
    }

    public static ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    public boolean connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                return true;
            }
            return true;
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendRequest(String action, String[] data) throws IOException {
        if (!connect()) {
            throw new IOException("Could not connect to server");
        }

        dos.writeUTF(action);
        for (String item : data) {
            dos.writeUTF(item);
        }
        dos.flush();

        String response = dis.readUTF();
        return response;
    }
}
