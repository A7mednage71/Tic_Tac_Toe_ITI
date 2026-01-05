package com.mycompany.tic_tac_toe_server.network;

import com.google.gson.Gson;
import com.mycompany.tic_tac_toe_server.database.UserDAO;
import com.mycompany.tic_tac_toe_server.models.RequestData;
import com.mycompany.tic_tac_toe_server.models.ResponseData;
import com.mycompany.tic_tac_toe_server.models.ResponseStatus;
import java.io.DataOutputStream;
import java.io.IOException;

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
        if (ServerThread.onlineUsers.containsKey(cleanUsername)) {
            response = new ResponseData(ResponseStatus.FAILURE, "ALREADY_LOGGED_IN");
        } else {
            clientHandler.setUsername(cleanUsername);
            
           
            UserDAO.getInstance().updateUserStatus(cleanUsername, "active"); 
            
            response = new ResponseData(ResponseStatus.SUCCESS, "Login successful");
        }
    } else {
        response = new ResponseData(ResponseStatus.FAILURE, "Invalid username or password");
    }
    sendResponse(response);
}

    private void sendResponse(ResponseData responseData) throws IOException {
        String jsonResponse = gson.toJson(responseData);
        dos.writeUTF(jsonResponse);
        dos.flush();
    }
}