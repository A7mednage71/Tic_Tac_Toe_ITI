package com.mycompany.finalprojectclient.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.google.gson.Gson;
import com.mycompany.finalprojectclient.models.RequestData;
import com.mycompany.finalprojectclient.models.RequestType;
import com.mycompany.finalprojectclient.utils.AppConstants;

public class ServerConnection {

    private static ServerConnection instance;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final Gson gson = new Gson();
    private BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    private NotificationListener notificationListener;
    private InviteListener inviteListener;
    private GameMoveListener gameMoveListener;
    private ScoreListener scoreListener;
    private Thread messageRouter;

    public interface NotificationListener {
        void onUserListUpdated();
    }

    public interface InviteListener {
        void onInviteReceived(String fromUsername);

        void onInviteAccepted(String username);

        void onInviteRejected(String username);

        void onOpponentWithdrew(String username);

        void onPlayAgainRequested(String username);

        void onGameStart(String symbol, String opponent);
    }

    private ServerConnection() {
    }

    public static ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    public java.net.Socket getSocket() {
        return socket;
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    public void setNotificationListener(NotificationListener listener) {
        this.notificationListener = listener;
    }

    public void setInviteListener(InviteListener listener) {
        this.inviteListener = listener;
    }

    private boolean connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(AppConstants.SERVER_HOST, AppConstants.SERVER_PORT);
                dis = new DataInputStream(socket.getInputStream());
                dos = new DataOutputStream(socket.getOutputStream());

                responseQueue.clear(); // Clear any stale responses
                startMessageRouter();
                System.out.println("Connected to server: " + AppConstants.SERVER_HOST);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Connection failed: " + e.getMessage());
            return false;
        }
    }

    private void startMessageRouter() {
        if (messageRouter != null && messageRouter.isAlive()) {
            return;
        }

        messageRouter = new Thread(() -> {
            try {
                while (socket != null && !socket.isClosed()) {
                    String message = dis.readUTF();

                    if ("USER_LIST_UPDATED".equals(message)) {
                        if (notificationListener != null) {
                            notificationListener.onUserListUpdated();
                        }
                    } else if (message.startsWith("INVITE_FROM:")) {
                        String fromUsername = message.substring("INVITE_FROM:".length());
                        if (inviteListener != null) {
                            inviteListener.onInviteReceived(fromUsername);
                        }
                    } else if (message.startsWith("INVITE_ACCEPTED:")) {
                        String username = message.substring("INVITE_ACCEPTED:".length());
                        if (inviteListener != null) {
                            inviteListener.onInviteAccepted(username);
                        }
                    } else if (message.startsWith("INVITE_REJECTED:")) {
                        String username = message.substring("INVITE_REJECTED:".length());
                        if (inviteListener != null) {
                            inviteListener.onInviteRejected(username);
                        }
                    } else if (message.startsWith("OPPONENT_WITHDREW:")) {
                        String username = message.substring("OPPONENT_WITHDREW:".length());
                        if (inviteListener != null) {
                            inviteListener.onOpponentWithdrew(username);
                        }
                    } else if (message.startsWith("PLAY_AGAIN_REQUESTED:")) {
                        String username = message.substring("PLAY_AGAIN_REQUESTED:".length());
                        if (inviteListener != null) {
                            inviteListener.onPlayAgainRequested(username);
                        }
                    } else if (message.startsWith("MOVE|")) {
                        String[] parts = message.split("\\|");
                        if (parts.length == 3 && gameMoveListener != null) {
                            int r = Integer.parseInt(parts[1]);
                            int c = Integer.parseInt(parts[2]);
                            gameMoveListener.onMoveReceived(r, c);
                        }
                    } else if (message.startsWith("GAME_START|")) {
                        String[] parts = message.split("\\|");
                        if (parts.length == 3 && inviteListener != null) {
                            inviteListener.onGameStart(parts[1], parts[2]);
                        }
                    } else if (message.startsWith("SCORE_UPDATE:")) {
                        System.out.println("=== SCORE_UPDATE received ===");
                        System.out.println("Raw message: " + message);
                        // Format: SCORE_UPDATE:username:score
                        String[] parts = message.substring("SCORE_UPDATE:".length()).split(":");
                        System.out.println("Parsed parts length: " + parts.length);
                        if (parts.length >= 2) {
                            System.out.println("Username: " + parts[0] + ", Score: " + parts[1]);
                        }
                        if (parts.length == 2 && scoreListener != null) {
                            String username = parts[0];
                            int score = Integer.parseInt(parts[1]);
                            System.out.println("Calling scoreListener.onScoreUpdate(" + username + ", " + score + ")");
                            scoreListener.onScoreUpdate(username, score);
                        } else {
                            System.out.println(
                                    "ERROR: parts.length=" + parts.length + ", scoreListener=" + scoreListener);
                        }
                    } else {
                        responseQueue.put(message);
                    }
                }
            } catch (Exception e) {
                System.err.println("Message router error: " + e.getMessage());
            }
        });
        messageRouter.setDaemon(true);
        messageRouter.start();
    }

    public void disconnect() {
        try {
            if (dos != null && socket != null && !socket.isClosed()) {
                RequestData req = new RequestData();
                req.key = RequestType.DISCONNECT;
                dos.writeUTF(gson.toJson(req));
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public interface GameMoveListener {
        void onMoveReceived(int r, int c);
    }

    /**
     * Listener للاستماع لتحديثات الـ scores من السيرفر
     */
    public interface ScoreListener {
        void onScoreUpdate(String username, int newScore);
    }

    public void setGameMoveListener(GameMoveListener listener) {
        this.gameMoveListener = listener;
    }

    public void setScoreListener(ScoreListener listener) {
        this.scoreListener = listener;
    }

    public void sendGameMove(int r, int c) {
        try {
            if (dos != null) {
                dos.writeUTF("MOVE|" + r + "|" + c);
                dos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * إرسال نتيجة اللعبة للسيرفر لتحديث الـ scores
     * 
     * @param winner اسم الفائز
     * @param loser  اسم الخاسر
     * @param result نوع النتيجة: WIN, DRAW, WITHDRAW
     */
    public void sendGameEnd(String winner, String loser, String result) {
        try {
            RequestData req = new RequestData();
            req.key = RequestType.GAME_END;
            req.username = winner;
            req.targetUsername = loser;
            req.status = result;
            sendRequest(req);
            System.out.println("Game result sent: " + winner + " vs " + loser + " - " + result);
        } catch (IOException e) {
            System.err.println("Failed to send game end: " + e.getMessage());
        }
    }

    /**
     * طلب score لاعب معين من السيرفر
     * السيرفر هيرد بـ SCORE_UPDATE message
     * 
     * @param username اسم اللاعب
     */
    public void requestUserScore(String username) {
        try {
            RequestData req = new RequestData();
            req.key = RequestType.GET_SCORE;
            req.username = username;
            sendRequest(req);
        } catch (IOException e) {
            System.err.println("Failed to request score for " + username + ": " + e.getMessage());
        }
    }

    public String sendRequest(Object requestObject) throws IOException {
        if (!connect()) {
            throw new IOException("Server is not reachable");
        }

        String json = gson.toJson(requestObject);

        dos.writeUTF(json);
        dos.flush();

        try {
            return responseQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted");
        }
    }
}
