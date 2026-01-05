package com.mycompany.tic_tac_toe_server.database;

public final class DatabaseConstants {

    private DatabaseConstants() {
    }

    // ==========================================================
    // Connection Config
    // ==========================================================
    public static final String DB_URL = "jdbc:derby://localhost:1527/TicTacToeDB";
    public static final String DB_USER = "app";
    public static final String DB_PASSWORD = "app";
    // ==========================================================
    // SQL Queries
    // ==========================================================
    public static final String CHECK_USER_QUERY = "SELECT USERNAME FROM APP.USERS WHERE USERNAME = ?";
    public static final String REGISTER_USER_QUERY = "INSERT INTO APP.USERS(USERNAME, PASSWORD) VALUES (?, ?)";
    public static final String LOGIN_USER_QUERY = "SELECT * FROM APP.USERS WHERE TRIM(USERNAME)=? AND TRIM(PASSWORD)=?";
}
