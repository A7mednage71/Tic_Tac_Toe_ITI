package com.mycompany.finalprojectserver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    static Connection con;

    static {
        try {
            
            con = DriverManager.getConnection(
                "jdbc:derby:FinalProjectSinup;create=true", "root", "root"
            );

            
            String createTable = "CREATE TABLE USERS ("
                    + "USERNAME VARCHAR(50) NOT NULL, "
                    + "EMAIL VARCHAR(100), "
                    + "PASSWORD VARCHAR(50) NOT NULL"
                    + ")";
            try {
                con.createStatement().executeUpdate(createTable);
            } catch (Exception e) {
               
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean register(String username, String email, String password) {
        try {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO USERS(USERNAME, EMAIL, PASSWORD) VALUES (?, ?, ?)"
            );
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean login(String username, String password) {
        try {
            PreparedStatement ps = con.prepareStatement(
                "SELECT * FROM USERS WHERE USERNAME=? AND PASSWORD=?"
            );
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
