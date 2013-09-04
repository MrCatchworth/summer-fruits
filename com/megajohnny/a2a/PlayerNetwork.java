package com.megajohnny.a2a;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

//a thread to handle all the networking worries, so the non-*Network classes don't have to
public class PlayerNetwork extends Thread {

    //talking to the other end stuff
    private final Socket endpoint;
    private final BufferedReader inStream;
    private final PrintWriter outStream;
    
    //player stuff
    private final Player player;
    
    //set this false to stop the thread stuff
    private boolean running;
    
    
    
    public PlayerNetwork(Socket endpoint) throws IOException {
        this.endpoint = endpoint;
        inStream = new BufferedReader(new InputStreamReader(endpoint.getInputStream()));
        outStream = new PrintWriter(endpoint.getOutputStream(), true);
        player = new Player(this);
        running = true;
    }
    
    //send a command with one argument (to avoid mucking about with arrays and loops)
    public void send(String cmd, String arg) {
        outStream.println(cmd+"\t"+arg);
    }
    //send a command with no arguments
    public void send(String cmd) {
        outStream.println(cmd);
    }
    //send a command with some arguments
    public void send(String cmd, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(cmd);
        for (String arg : args) {
            sb.append("\t");
            sb.append(arg);
        }
        outStream.println(sb.toString());
    }
    
    public Player getPlayer() {
        return player;
    }
    
    private void processMessage(String s) {
        String[] cmd_and_args = s.split("\t", 2);
        
        String cmd = cmd_and_args[0];
        String[] args = null;
        
        if (cmd_and_args.length == 1) {
            args = new String[0];
        } else {
            args = cmd_and_args[1].split("\t");
        }
        
        player.processMessage(cmd, args);
    }
    
    public void disconnect() {
        if (player.isRegistered()) Game.currentGame.getLogic().playerLeaves(player);
        Game.currentGame.getNetwork().disconnection(this);
        try {endpoint.close();}catch(IOException e){/* no idea what to do if close fails */}
        running = false;
    }
    
    public void readError() {
        disconnect();
    }
    
    public void run() {
        send("Hello");
        try {
            while (running) {
                String line = inStream.readLine();
                if (line == null) {
                    break;
                }
                processMessage(line);
            }
        }
        catch (IOException e) {
            readError();
        }
    }
}