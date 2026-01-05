package com.mycompany.tic_tac_toe_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

//  act as security officer
public class ServerThread extends Thread {

    private static final int PORT = 5002;
    private ServerSocket serverSocket;

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            System.out.println("Waiting for clients...");

            while (!serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket.getInetAddress());
                    // act as Customer Service Employee
                    new ClientHandler(socket).start();
                } catch (SocketException e) {
                    System.out.println("Server socket closed via Stop button.");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("Server Error: " + e.getMessage());
        } finally {
            stopServer();
        }
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            System.out.println("Server stopped.");
        } catch (IOException e) {
            System.err.println("Error stopping server: " + e.getMessage());
        }
    }
}
