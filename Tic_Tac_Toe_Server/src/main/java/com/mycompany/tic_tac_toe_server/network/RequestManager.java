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

            case GET_ONLINE_USERS:
                handleGetOnlineUsers();
                break;

            case SEND_INVITE:
                handleSendInvite(request);
                break;

            case ACCEPT_INVITE:
                handleAcceptInvite(request);
                break;

            case REJECT_INVITE:
                handleRejectInvite(request);
                break;

            case UPDATE_STATUS:
                handleUpdateStatus(request);
                break;

            case WITHDRAW:
                handleWithdraw(request);
                break;
            case PLAY_AGAIN:
                handlePlayAgain(request);
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
                clientHandler.setStatus("active");

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
        java.util.Map<String, String> onlineUsersMap = new java.util.HashMap<>();
        for (ClientHandler client : ServerThread.onlineUsers) {
            if (client.getUsername() != null) {
                onlineUsersMap.put(client.getUsername(), client.getStatus());
            }
        }
        ResponseData response = new ResponseData(ResponseStatus.SUCCESS, gson.toJson(onlineUsersMap));
        sendResponse(response);
    }

    private void handleSendInvite(RequestData req) throws IOException {
        String fromUsername = req.username;
        String targetUsername = req.targetUsername;

        System.out.println("Invite from " + fromUsername + " to " + targetUsername);

        ClientHandler targetClient = findClientByUsername(targetUsername);
        if (targetClient != null) {
            targetClient.sendInvite(fromUsername);
            ResponseData response = new ResponseData(ResponseStatus.SUCCESS, "Invite sent");
            sendResponse(response);
        } else {
            ResponseData response = new ResponseData(ResponseStatus.FAILURE, "User not found");
            sendResponse(response);
        }
    }

    private void handleAcceptInvite(RequestData req) throws IOException {
        String acceptingUsername = req.username;
        String inviterUsername = req.targetUsername;

        ClientHandler inviterClient = findClientByUsername(inviterUsername);

        if (inviterClient == null) {
            sendResponse(new ResponseData(ResponseStatus.FAILURE, "Inviter is offline"));
            return;
        }

        if ("in_game".equalsIgnoreCase(inviterClient.getStatus())) {
            if (this.clientHandler.getOpponent() != inviterClient) {
                sendResponse(new ResponseData(ResponseStatus.FAILURE, "Inviter is already in a game"));
                return;
            }
        }
        this.clientHandler.setOpponent(inviterClient);
        inviterClient.setOpponent(this.clientHandler);

        this.clientHandler.setStatus("in_game");
        inviterClient.setStatus("in_game");

        UserDAO.getInstance().updateUserStatus(acceptingUsername, "in_game");
        UserDAO.getInstance().updateUserStatus(inviterUsername, "in_game");

        ServerThread.broadcastUserListUpdate();

        inviterClient.sendInviteAccepted(acceptingUsername); 
        inviterClient.sendMessage("GAME_START|X|" + acceptingUsername);
        this.clientHandler.sendMessage("GAME_START|O|" + inviterUsername);

        sendResponse(new ResponseData(ResponseStatus.SUCCESS, "Invite accepted"));
    }

    private void handleRejectInvite(RequestData req) throws IOException {
        String rejectingUsername = req.username;
        String inviterUsername = req.targetUsername;

        System.out.println(rejectingUsername + " rejected invite from " + inviterUsername);

        ClientHandler inviterClient = findClientByUsername(inviterUsername);
        if (inviterClient != null) {
            inviterClient.sendInviteRejected(rejectingUsername);
        }

        ResponseData response = new ResponseData(ResponseStatus.SUCCESS, "Invite rejected");
        sendResponse(response);
    }

    private void handleUpdateStatus(RequestData req) throws IOException {
        String newStatus = req.status; 
        if (newStatus == null) {
            return;
        }

        clientHandler.setStatus(newStatus);
        UserDAO.getInstance().updateUserStatus(clientHandler.getUsername(), newStatus);

        System.out.println("Status updated to " + newStatus + " for " + clientHandler.getUsername());

        ResponseData response = new ResponseData(ResponseStatus.SUCCESS, "Status updated");
        sendResponse(response);

        ServerThread.broadcastUserListUpdate();
    }

    private void handleWithdraw(RequestData req) throws IOException {
        String fromUser = req.username;
        String targetUser = req.targetUsername;

        System.out.println(fromUser + " withdrew from game against " + targetUser);

        clientHandler.setOpponent(null);
        clientHandler.setStatus("active");
        UserDAO.getInstance().updateUserStatus(fromUser, "active");

        ClientHandler targetClient = findClientByUsername(targetUser);

        if (targetClient != null) {
            targetClient.setOpponent(null);
            targetClient.sendWithdrawNotification(fromUser);
        }

        ServerThread.broadcastUserListUpdate();
        ResponseData response = new ResponseData(ResponseStatus.SUCCESS, "Withdrawn");
        sendResponse(response);
    }

    private void handlePlayAgain(RequestData req) throws IOException {
        String fromUser = req.username;
        String targetUser = req.targetUsername;
        System.out.println(fromUser + " wants to play again with " + targetUser);

        ClientHandler targetClient = findClientByUsername(targetUser);
        if (targetClient != null) {
            targetClient.sendMessage("PLAY_AGAIN_REQUESTED:" + fromUser);
            sendResponse(new ResponseData(ResponseStatus.SUCCESS, "Request sent"));
        } else {
            sendResponse(new ResponseData(ResponseStatus.FAILURE, "User offline"));
        }
    }

    private ClientHandler findClientByUsername(String username) {
        for (ClientHandler client : ServerThread.onlineUsers) {
            if (client.getUsername() != null && client.getUsername().equals(username)) {
                return client;
            }
        }
        return null;
    }

    private void sendResponse(ResponseData responseData) throws IOException {
        String jsonResponse = gson.toJson(responseData);
        dos.writeUTF(jsonResponse);
        dos.flush();
    }
}