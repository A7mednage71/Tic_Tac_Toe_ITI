package com.mycompany.tic_tac_toe_server.network;

import java.io.DataOutputStream;
import java.io.IOException;

import com.google.gson.Gson;
import com.mycompany.tic_tac_toe_server.database.DatabaseConstants;
import com.mycompany.tic_tac_toe_server.database.UserDAO;
import com.mycompany.tic_tac_toe_server.models.RequestData;
import com.mycompany.tic_tac_toe_server.models.ResponseData;
import com.mycompany.tic_tac_toe_server.models.ResponseStatus;

public class RequestManager {

    private final DataOutputStream dos;
    private final ClientHandler clientHandler;
    private final Gson gson = new Gson();
    private static int scoreUpdateSequence = 0;

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
            case GAME_END:
                handleGameEnd(request);
                break;
            case GET_SCORE:
                handleGetScore(request);
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

        String username = clientHandler.getUsername();
        clientHandler.setStatus(newStatus);
        UserDAO.getInstance().updateUserStatus(username, newStatus);

        System.out.println("Status updated to " + newStatus + " for " + username);

        ResponseData response = new ResponseData(ResponseStatus.SUCCESS, "Status updated");
        sendResponse(response);

        // Broadcast status update to all clients
        String statusUpdateMessage = "STATUS_UPDATE:" + username + ":" + newStatus;
        System.out.println("Broadcasting: " + statusUpdateMessage);
        for (ClientHandler client : ServerThread.onlineUsers) {
            if (client != null) {
                try {
                    client.sendMessage(statusUpdateMessage);
                    System.out.println("Status update sent to: " + client.getUsername());
                } catch (Exception e) {
                    System.err.println("Error sending status update to client: " + e.getMessage());
                }
            }
        }

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

    private void handleGameEnd(RequestData req) throws IOException {
        String winner = req.username;
        String loser = req.targetUsername;
        String result = req.status;

        System.out.println("Game ended: " + winner + " vs " + loser + " - Result: " + result);

        UserDAO userDAO = UserDAO.getInstance();

        if ("WIN".equals(result)) {
            userDAO.updateUserScore(winner, DatabaseConstants.SCORE_WIN);
            userDAO.updateUserScore(loser, DatabaseConstants.SCORE_LOSE);

        } else if ("DRAW".equals(result)) {
            userDAO.updateUserScore(winner, DatabaseConstants.SCORE_DRAW);
            userDAO.updateUserScore(loser, DatabaseConstants.SCORE_DRAW);

        } else if ("WITHDRAW".equals(result)) {
            // When a player withdraws, the "winner" is actually the player who withdrew (should be penalized)
            // and the "loser" is the player who stayed (should be rewarded)
            // This naming is counter-intuitive, so we need to reverse it
            userDAO.updateUserScore(winner, DatabaseConstants.SCORE_WITHDRAW); // Player who withdrew gets penalty
            userDAO.updateUserScore(loser, DatabaseConstants.SCORE_WIN);      // Player who stayed gets win bonus
        }

        broadcastScoreUpdates(winner, loser);

        sendResponse(new ResponseData(ResponseStatus.SUCCESS, "Scores updated"));
    }

    private void broadcastScoreUpdates(String player1, String player2) {
        try {
            UserDAO userDAO = UserDAO.getInstance();
            int score1 = userDAO.getUserScore(player1);
            int score2 = userDAO.getUserScore(player2);

            ClientHandler client1 = findClientByUsername(player1);
            ClientHandler client2 = findClientByUsername(player2);

            // Increment sequence number for this score update
            int currentSequence = ++scoreUpdateSequence;

            // Create consistent score update messages with sequence numbers
            String scoreUpdate1 = "SCORE_UPDATE:" + currentSequence + ":" + player1 + ":" + score1;
            String scoreUpdate2 = "SCORE_UPDATE:" + currentSequence + ":" + player2 + ":" + score2;

            // Send updates to both clients in a consistent order
            if (client1 != null) {
                client1.sendMessage(scoreUpdate1);
                client1.sendMessage(scoreUpdate2);
            }

            if (client2 != null) {
                client2.sendMessage(scoreUpdate1);
                client2.sendMessage(scoreUpdate2);
            }

            System.out.println("Broadcasted scores (seq=" + currentSequence + "): " + player1 + "=" + score1 + ", " + player2 + "=" + score2);
        } catch (Exception e) {
            System.err.println("Error broadcasting scores: " + e.getMessage());
        }
    }

    private void handleGetScore(RequestData req) throws IOException {
        System.out.println("=== handleGetScore ===");
        System.out.println("Requesting client: " + this.clientHandler.getUsername());
        System.out.println("Requested username: " + req.username);

        try {
            String username = req.username;
            int score = UserDAO.getInstance().getUserScore(username);

            System.out.println("Retrieved score for " + username + ": " + score);

            // Use sequence number for consistency
            int currentSequence = ++scoreUpdateSequence;
            String message = "SCORE_UPDATE:" + currentSequence + ":" + username + ":" + score;
            System.out.println("Sending to client " + this.clientHandler.getUsername() + ": " + message);
            this.clientHandler.sendMessage(message);

            ResponseData response = new ResponseData(ResponseStatus.SUCCESS, "Score retrieved");
            sendResponse(response);
        } catch (Exception e) {
            System.err.println("Error in handleGetScore: " + e.getMessage());
            e.printStackTrace();
            ResponseData response = new ResponseData(ResponseStatus.FAILURE, "Error getting score: " + e.getMessage());
            sendResponse(response);
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