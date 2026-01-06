package com.mycompany.finalprojectclient.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.Gson;
import com.mycompany.finalprojectclient.models.RequestData;
import com.mycompany.finalprojectclient.models.RequestType;

public class ServerConnection {

    private static ServerConnection instance;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final Gson gson = new Gson();
    private BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private NotificationListener notificationListener;
    private Thread messageRouter;

    public interface NotificationListener {
        void onUserListUpdated();
    }

    private ServerConnection() {
    }

    public static ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    public java.net.Socket getSocket() {
        return socket;
    }

    public void setNotificationListener(NotificationListener listener) {
        this.notificationListener = listener;
    }

    private boolean connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket("localhost", 5002);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());
                startMessageRouter();
                System.out.println("Connected to server");
            }
            return true;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    private void startMessageRouter() {
        if (messageRouter != null && messageRouter.isAlive()) {
            return;
        }

        messageRouter = new Thread(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    String message = dis.readUTF();

                    if ("USER_LIST_UPDATED".equals(message)) {
                        if (notificationListener != null) {
                            notificationListener.onUserListUpdated();
                        }
                    } else {
                        responseQueue.put(message);
                    }
                }
            } catch (Exception e) {
                System.err.println("Message router error: " + e.getMessage());
            }
        });
        messageRouter.setDaemon(true);
        messageRouter.start();
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

        try {
            return responseQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted");
        }
    }
}
