/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.finalprojectserver;

import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Shady
 */
public class FinalprojectServer {

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(5000);
            System.out.println("Server started...");

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
