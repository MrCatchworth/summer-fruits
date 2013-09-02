package com.megajohnny.a2a;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

public class PlayerNetworkTest {
    public static void main(String[] args) {
        PlayerNetwork pn = null;
        try {
            System.out.println("Starting to listen");
            ServerSocket server = new ServerSocket(4746);
            Socket client = server.accept();
            System.out.println("Someone connected");
            pn = new PlayerNetwork(client);
            pn.start();
            try {pn.join();} catch(InterruptedException e) {}
        }
        catch (IOException e) {
            System.out.println("Exception");
            e.printStackTrace();
        }
        finally {
            pn.disconnect();
        }
        
        System.out.println("Goodbye");
    }
}