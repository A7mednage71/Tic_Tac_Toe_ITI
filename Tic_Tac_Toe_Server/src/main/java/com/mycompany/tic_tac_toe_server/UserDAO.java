package com.mycompany.tic_tac_toe_server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    private static UserDAO instance;
    private Connection con;

    private UserDAO() {
        try {
            String url = "jdbc:derby://localhost:1527/TicTacToeDB";
            con = DriverManager.getConnection(url, "app", "app");
            System.out.println("Connected to TicTacToeDB successfully");
        } catch (Exception e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
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

    public boolean register(String username, String password) {
        if (con == null) {
            System.err.println("Database connection is null");
            return false;
        }

        try {
            PreparedStatement checkPs = con.prepareStatement(
                    "SELECT * FROM APP.USERS WHERE USERNAME=?");
            checkPs.setString(1, username);
            ResultSet checkRs = checkPs.executeQuery();

            if (checkRs.next()) {
                System.out.println("Username already exists: " + username);
                return false;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO APP.USERS(USERNAME, PASSWORD) VALUES (?, ?)");
            ps.setString(1, username);
            ps.setString(2, password);
            ps.executeUpdate();
            System.out.println("User registered successfully: " + username);
            return true;
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean login(String username, String password) {
        if (con == null) {
            System.err.println("Database connection is null");
            return false;
        }

        try {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM APP.USERS WHERE USERNAME=? AND PASSWORD=? AND STATUS='Active'");
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            boolean success = rs.next();

            if (success) {
                System.out.println("Login successful for user: " + username);
            } else {
                System.out.println("Login failed for user: " + username);
            }

            return success;
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
