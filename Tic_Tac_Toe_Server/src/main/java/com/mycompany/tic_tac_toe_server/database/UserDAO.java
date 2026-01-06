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
        if (con == null) {
            System.err.println("Registration failed: No database connection.");
            return false;
        }

        try {
            if (isUserExists(username)) {
                System.out.println("Username '" + username + "' is already taken.");
                return false;
            }

            try (PreparedStatement ps = con.prepareStatement(DatabaseConstants.REGISTER_USER_QUERY)) {
                ps.setString(1, username);
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
        if (con == null) {
            System.err.println("Login failed: No database connection.");
            return false;
        }

        try (PreparedStatement ps = con.prepareStatement(DatabaseConstants.LOGIN_USER_QUERY)) {

            ps.setString(1, username);

            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                boolean success = rs.next();

                if (success) {
                    System.out.println("Login successful for user: " + username);
                } else {
                    System.out.println("Login failed for user: " + username);
                }
                return success;
            }

        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            return false;
        }
    }

    // ===================== Update status in database ============

    public void updateUserStatus(String username, String status) {

        try (PreparedStatement pstmt = con.prepareStatement(DatabaseConstants.UPDATE_USER_STATUS)) {
            pstmt.setString(1, status.toLowerCase());
            pstmt.setString(2, username);

            int rowsAffected = pstmt.executeUpdate();
            // return 1 if user found
            if (rowsAffected > 0) {
                System.out.println("DB Success: Status updated to " + status + " for " + username);
            } else {
                System.out.println("DB Warning: User " + username + " not found in APP.ROOT table.");
            }
        } catch (SQLException e) {
            System.err.println("DB Error Detail: " + e.getMessage());
        }
    }

    // ================ Get All Players From database ================

    public List<PlayerModel> getAllPlayers() throws SQLException {
        List<PlayerModel> players = new ArrayList<>();

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
