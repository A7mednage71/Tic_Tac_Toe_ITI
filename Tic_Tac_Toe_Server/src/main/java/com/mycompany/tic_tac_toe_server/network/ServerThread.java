package com.mycompany.tic_tac_toe_server.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

import com.mycompany.tic_tac_toe_server.database.UserDAO;

public class ServerThread extends Thread {

    private static final int PORT = 5002;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public static final Vector<ClientHandler> onlineUsers = new Vector<ClientHandler>();

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("Server started on port " + PORT);

            while (running && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket.getInetAddress());
                    new ClientHandler(socket).start();
                } catch (SocketException e) {
                    if (!running) {
                        System.out.println("Server socket closed via Stop button.");
                    }
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    public void stopServer() {
        running = false;

        try {
            // Close server socket first to stop accepting new connections
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server socket closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        // Then logout all connected users
        logoutAllUsers();
    }

    private void cleanup() {
        if (running) {
            running = false;
            logoutAllUsers();
        }
    }

    private void logoutAllUsers() {
        try {
            System.out.println("Logging out all connected users...");
            for (ClientHandler client : onlineUsers) {
                try {
                    if (client.getUsername() != null) {
                        System.out.println("Logging out user: " + client.getUsername());
                        UserDAO.getInstance()
                                .updateUserStatus(client.getUsername(), "Disactive");
                    }
                    client.closeConnection();
                } catch (Exception e) {
                    System.err.println("Error logging out client: " + e.getMessage());
                }
            }

            onlineUsers.clear();
            System.out.println("All users logged out. Server stopped.");
        } catch (Exception e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    public static void broadcastUserListUpdate() {
        for (ClientHandler client : onlineUsers) {
            try {
                client.sendUserListUpdate();
            } catch (Exception e) {
                System.err.println("Error broadcasting to client: " + e.getMessage());
            }
        }
    }
}