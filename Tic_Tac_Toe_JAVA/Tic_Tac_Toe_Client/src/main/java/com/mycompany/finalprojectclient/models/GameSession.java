package com.mycompany.finalprojectclient.models;

public class GameSession {
    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public static boolean vsComputer = false;
    public static Difficulty difficulty = Difficulty.EASY;

    public static boolean isOnline = false;
    public static String opponentName = "";
    public static String playerSymbol = "X";
    public static int opponentScore = 0;

    public static boolean isReplay = false;
    public static String replayFilePath = "";
    public static boolean isHistoryReplay = false;  

    public static String previousScreen = "";
}
