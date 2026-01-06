package com.mycompany.finalprojectclient.models;

public class GameSession {
    public enum Difficulty { EASY, MEDIUM, HARD }
    
    public static boolean vsComputer = false;
    public static Difficulty difficulty = Difficulty.EASY;
    
    // Online Play Fields
    public static boolean isOnline = false;
    public static String opponentName = "";
    public static String playerSymbol = "X"; // Default
}
