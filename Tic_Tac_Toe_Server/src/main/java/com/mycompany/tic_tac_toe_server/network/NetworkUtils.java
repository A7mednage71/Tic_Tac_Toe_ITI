/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tic_tac_toe_server.network;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class NetworkUtils {
    public static String getServerIP() {
        try (final DatagramSocket socket = new DatagramSocket()) {
            // We don't actually connect, but this "tricks" the OS 
            // into telling us which interface it would use to reach the internet.
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1"; // Fallback to localhost if offline
        }
    }
}