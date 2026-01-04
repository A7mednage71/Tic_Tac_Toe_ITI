package com.mycompany.tic_tac_toe_server;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {

    private static final int PORT = 5002;
    private volatile boolean running = true;

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for clients...");

            while (running) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getInetAddress());
                new ClientHandler(socket).start();
            }

            serverSocket.close();
        } catch (Exception e) {
            if (running) {
                System.err.println("Server error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stopServer() {
        running = false;
        System.out.println("Server stopping...");
    }
}
