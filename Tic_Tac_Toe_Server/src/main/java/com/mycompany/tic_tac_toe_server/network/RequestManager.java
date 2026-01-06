package com.mycompany.tic_tac_toe_server.network;

import com.google.gson.Gson;
import com.mycompany.tic_tac_toe_server.database.UserDAO;
import com.mycompany.tic_tac_toe_server.models.RequestData;
import com.mycompany.tic_tac_toe_server.models.ResponseData;
import com.mycompany.tic_tac_toe_server.models.ResponseStatus;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class RequestManager {

    private final DataOutputStream dos;
    private final ClientHandler clientHandler;
    private final Gson gson = new Gson();

    public RequestManager(DataOutputStream dos, ClientHandler clientHandler) {
        this.dos = dos;
        this.clientHandler = clientHandler;
    }

    public void processRequest(RequestData request) throws IOException {
        if (request.key == null) {
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
                clientHandler.closeConnection();
                break;

            case GET_ONLINE_USERS:
                handleGetOnlineUsers();
                break;
        }
    }

    private void handleRegister(RequestData req) throws IOException {
        boolean success = UserDAO.getInstance().register(req.username, req.password);
        ResponseData response;
        if (success) {
            response = new ResponseData(ResponseStatus.SUCCESS, "Account created successfully");
        } else {
            response = new ResponseData(ResponseStatus.FAILURE, "Username already exists");
        }
        sendResponse(response);
    }

    private void handleLogin(RequestData req) throws IOException {
        String cleanUsername = req.username.trim().toLowerCase();
        boolean isValid = UserDAO.getInstance().login(cleanUsername, req.password);
        ResponseData response;

        if (isValid) {
            boolean alreadyLoggedIn = false;
            for (ClientHandler client : ServerThread.onlineUsers) {
                if (client.getUsername() != null && client.getUsername().equals(cleanUsername)) {
                    alreadyLoggedIn = true;
                    break;
                }
            }

            if (alreadyLoggedIn) {
                response = new ResponseData(ResponseStatus.FAILURE, "ALREADY_LOGGED_IN");
            } else {
                clientHandler.setUsername(cleanUsername);

                UserDAO.getInstance().updateUserStatus(cleanUsername, "active");

                response = new ResponseData(ResponseStatus.SUCCESS, "Login successful");
                sendResponse(response);

                ServerThread.broadcastUserListUpdate();
                return;
            }
        } else {
            response = new ResponseData(ResponseStatus.FAILURE, "Invalid username or password");
        }
        sendResponse(response);
    }

    private void handleGetOnlineUsers() throws IOException {
        List<String> onlineUsersList = new ArrayList<>();
        for (ClientHandler client : ServerThread.onlineUsers) {
            if (client.getUsername() != null) {
                onlineUsersList.add(client.getUsername());
            }
        }
        ResponseData response = new ResponseData(ResponseStatus.SUCCESS, gson.toJson(onlineUsersList));
        sendResponse(response);
    }

    private void sendResponse(ResponseData responseData) throws IOException {
        String jsonResponse = gson.toJson(responseData);
        dos.writeUTF(jsonResponse);
        dos.flush();
    }
}