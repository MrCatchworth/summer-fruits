package com.megajohnny.a2a;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;

public class GameNetwork {
    private final Set<PlayerNetwork> connections;
    private final Game game;
    
    public GameNetwork(Game g) {
        connections = new HashSet<PlayerNetwork>();
        game = g;
    }
    
    public synchronized void newConnection(Socket endpoint) {
        PlayerNetwork pn = null;
        try {
            pn = new PlayerNetwork(endpoint);
        }
        catch (IOException e) {
            try {endpoint.close(); return;} catch(IOException e2){}
        }
        System.out.println("A player has connected");
        connections.add(pn);
        pn.start();
    }
    
    public synchronized void disconnection(PlayerNetwork pn) {
        connections.remove(pn);
    }
}