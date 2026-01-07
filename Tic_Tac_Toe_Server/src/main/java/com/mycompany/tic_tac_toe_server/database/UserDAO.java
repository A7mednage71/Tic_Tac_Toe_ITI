package com.mycompany.tic_tac_toe_server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.tic_tac_toe_server.models.PlayerModel;

public class UserDAO {

    private static UserDAO instance;
    private Connection con;

    private UserDAO() {
        try {
            con = DriverManager.getConnection(
                    DatabaseConstants.DB_URL,
                    DatabaseConstants.DB_USER,
                    DatabaseConstants.DB_PASSWORD);

            System.out.println("Connected to Tic_Tac_Toe_DB successfully");
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
    }

    public static UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    public Connection getConnection() {
        return con;
    }

    // ===================== Register Function =================================
    public boolean register(String username, String password) {
        if (con == null) return false;

        try {
            if (isUserExists(username)) return false;

            try (PreparedStatement ps = con.prepareStatement(DatabaseConstants.REGISTER_USER_QUERY)) {
                ps.setString(1, username.toLowerCase());
                ps.setString(2, password);
                ps.executeUpdate();

                System.out.println("User registered successfully: " + username);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    // ===================== Check UserName availabilty Function ===============
    private boolean isUserExists(String username) throws SQLException {
        try (PreparedStatement checkPs = con.prepareStatement(DatabaseConstants.CHECK_USER_QUERY)) {
            checkPs.setString(1, username);
            try (ResultSet rs = checkPs.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ===================== Login Function =====================
    public boolean login(String username, String password) {
        if (con == null) return false;

        try (PreparedStatement ps = con.prepareStatement(DatabaseConstants.LOGIN_USER_QUERY)) {
            ps.setString(1, username.toLowerCase());
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    // ===================== Update status in database ============
    // This is called by RequestManager during Invites and Withdraws
    public void updateUserStatus(String username, String status) {
        if (con == null || username == null) return;

        try (PreparedStatement pstmt = con.prepareStatement(DatabaseConstants.UPDATE_USER_STATUS)) {
            pstmt.setString(1, status.toLowerCase());
            pstmt.setString(2, username.toLowerCase());

            int rowsAffected = pstmt.executeUpdate();
            // return 1 if user found
            if (rowsAffected > 0) {
                System.out.println("DB Success: " + username + " is now " + status);
            }
        } catch (SQLException e) {
            System.err.println("DB Update Error: " + e.getMessage());
        }
    }

    // ===================== Update Score (New Method) ============
    // You can call this from RequestManager when a "MOVE" results in a win
    public void incrementUserScore(String username) {
        String query = "UPDATE APP.USERS SET SCORE = SCORE + 10 WHERE LOWER(TRIM(USERNAME)) = ?";
        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setString(1, username.toLowerCase());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to update score: " + e.getMessage());
        }
    }

    // ================ Get All Players From database ================
    public List<PlayerModel> getAllPlayers() throws SQLException {
        List<PlayerModel> players = new ArrayList<>();
        if (con == null) return players;

        try (PreparedStatement ps = con.prepareStatement(DatabaseConstants.GET_ALL_PLAYERS)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                players.add(new PlayerModel(
                        rs.getString("USERNAME"),
                        rs.getInt("SCORE"),
                        rs.getString("STATUS")));
            }
        }
        System.err.println("Players Number = : " + players.size());
        return players;
    }
}