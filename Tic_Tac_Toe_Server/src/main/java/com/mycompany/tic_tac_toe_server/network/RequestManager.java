package com.mycompany.tic_tac_toe_server.network;

import java.io.DataOutputStream;
import java.io.IOException;

import com.mycompany.tic_tac_toe_server.database.UserDAO;
import com.mycompany.tic_tac_toe_server.models.RequestData;

public class RequestManager {

    private final DataOutputStream dos;
    private final ClientHandler clientHandler;

    public RequestManager(DataOutputStream dos, ClientHandler clientHandler) {
        this.dos = dos;
        this.clientHandler = clientHandler;
    }

    public void processRequest(RequestData request) throws IOException {
        if (request.key == null) {
            System.out.println("Received unknown or invalid request key.");
            return;
        }
        switch (request.key) {
            case REGISTER:
                handleRegister(request);
                break;

            case LOGIN:
                handleLogin(request);
                break;

            case DISCONNECT:
                System.out.println("Client requested disconnect");
                clientHandler.closeConnection();
                break;
        }
    }

    private void handleRegister(RequestData req) throws IOException {
        System.out.println("Register attempt: " + req.username);
        boolean success = UserDAO.getInstance().register(req.username, req.password);
        sendResponse(success ? "REGISTER_SUCCESS" : "REGISTER_FAIL");
    }

    private void handleLogin(RequestData req) throws IOException {
        System.out.println("Login attempt: " + req.username);
        boolean success = UserDAO.getInstance().login(req.username, req.password);
        sendResponse(success ? "LOGIN_SUCCESS" : "LOGIN_FAIL");
    }

    private void sendResponse(String msg) throws IOException {
        dos.writeUTF(msg);
        dos.flush();
        System.out.println("Sent response: " + msg);
    }
}
