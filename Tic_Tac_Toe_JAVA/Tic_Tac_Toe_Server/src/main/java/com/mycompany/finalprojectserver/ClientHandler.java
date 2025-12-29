package com.mycompany.finalprojectserver;

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

            String action = dis.readUTF();

            if (action.equals("REGISTER")) {
                String username = dis.readUTF();
                String email = dis.readUTF();
                String password = dis.readUTF();
                boolean success = UserDAO.register(username, email, password);
                dos.writeUTF(success ? "REGISTER_SUCCESS" : "REGISTER_FAIL");
            }

            if (action.equals("LOGIN")) {
                String username = dis.readUTF();
                String password = dis.readUTF();
                boolean success = UserDAO.login(username, password);
                dos.writeUTF(success ? "LOGIN_SUCCESS" : "LOGIN_FAIL");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (Exception e) {}
        }
    }
}
