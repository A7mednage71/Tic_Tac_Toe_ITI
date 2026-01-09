package com.mycompany.tic_tac_toe_server.database;

public final class DatabaseConstants {

    private DatabaseConstants() {
    }

    // ==========================================================
    // Connection Config
    // ==========================================================
    public static final String DB_URL = "jdbc:derby://localhost:1527/Tic_Tac_Toe_DB;create = true;";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "root";
    // ==========================================================
    // SQL Queries
    // ==========================================================
    public static final String CHECK_USER_QUERY = "SELECT USERNAME FROM APP.USERS WHERE USERNAME = ?";
    public static final String REGISTER_USER_QUERY = "INSERT INTO APP.USERS(USERNAME, PASSWORD) VALUES (?, ?)";
    public static final String LOGIN_USER_QUERY = "SELECT * FROM APP.USERS WHERE LOWER(TRIM(USERNAME))=? AND TRIM(PASSWORD)=?";
    public static final String UPDATE_USER_STATUS = "UPDATE APP.USERS SET STATUS = ? WHERE LOWER(TRIM(USERNAME)) = ?";
    public static final String GET_ALL_PLAYERS = "SELECT USERNAME, SCORE, STATUS FROM APP.USERS";
    public static final String UPDATE_USER_SCORE = "UPDATE APP.USERS SET SCORE = SCORE + ? WHERE LOWER(TRIM(USERNAME)) = ?";
    public static final String GET_USER_SCORE = "SELECT SCORE FROM APP.USERS WHERE LOWER(TRIM(USERNAME)) = ?";
    
    // ==========================================================
    // Score System Constants
    // ==========================================================
    public static final int SCORE_WIN = 10;
    public static final int SCORE_LOSE = -5;
    public static final int SCORE_DRAW = 3;
    public static final int SCORE_WITHDRAW = -15;

}
