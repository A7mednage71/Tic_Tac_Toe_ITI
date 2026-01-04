package com.mycompany.tic_tac_toe_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {
    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String action = dis.readUTF();
                System.out.println("Received action: " + action);

                if (action.equals("REGISTER")) {
                    String username = dis.readUTF();
                    String password = dis.readUTF();
                    System.out.println("Registration attempt for: " + username);
                    boolean success = UserDAO.getInstance().register(username, password);
                    dos.writeUTF(success ? "REGISTER_SUCCESS" : "REGISTER_FAIL");
                    dos.flush();
                    System.out.println("Sent response: " + (success ? "REGISTER_SUCCESS" : "REGISTER_FAIL"));
                }

                if (action.equals("LOGIN")) {
                    String username = dis.readUTF();
                    String password = dis.readUTF();
                    System.out.println("Login attempt for: " + username);
                    boolean success = UserDAO.getInstance().login(username, password);
                    dos.writeUTF(success ? "LOGIN_SUCCESS" : "LOGIN_FAIL");
                    dos.flush();
                    System.out.println("Sent response: " + (success ? "LOGIN_SUCCESS" : "LOGIN_FAIL"));
                }

                if (action.equals("DISCONNECT")) {
                    System.out.println("Client requested disconnect");
                    break;
                }
            }

        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("Client disconnected");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
