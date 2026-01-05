package com.mycompany.finalprojectclient.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.google.gson.Gson;
import com.mycompany.finalprojectclient.models.RequestData;
import com.mycompany.finalprojectclient.models.RequestType;
import com.mycompany.finalprojectclient.utils.AppConstants;

public class ServerConnection {

    private static ServerConnection instance;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final Gson gson = new Gson();

    private ServerConnection() {
    }

    public static ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    private boolean connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(AppConstants.SERVER_HOST, AppConstants.SERVER_PORT);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                return true;
            }
            return true;
        } catch (IOException e) {
            System.err.println("Connection Failed: " + e.getMessage());
            return false;
        }
    }

    public void disconnect() {
        try {
            if (dos != null && socket != null && !socket.isClosed()) {
                RequestData req = new RequestData();
                req.key = RequestType.DISCONNECT;
                dos.writeUTF(gson.toJson(req));
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String sendRequest(Object requestObject) throws IOException {
        if (!connect()) {
            throw new IOException("Server is not reachable");
        }

        String json = gson.toJson(requestObject);

        dos.writeUTF(json);
        dos.flush();

        return dis.readUTF();
    }
}
