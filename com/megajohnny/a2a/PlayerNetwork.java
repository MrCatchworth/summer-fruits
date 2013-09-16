package com.megajohnny.a2a;

import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
    
    public static final char msg_delimiter = '\t';
    public static final String msg_delimiter_s = ""+msg_delimiter;
    public static final int timeout_sec = 0;
    
    public PlayerNetwork(Socket endpoint) throws IOException {
        this.endpoint = endpoint;
        inStream = new BufferedReader(new InputStreamReader(endpoint.getInputStream()));
        outStream = new PrintWriter(endpoint.getOutputStream(), true);
        player = new Player(this);
        running = true;
        endpoint.setSoTimeout(timeout_sec*1000);
    }
    
    //send a command with one argument (to avoid mucking about with arrays and loops)
    public void send(String cmd, String arg) {
        outStream.println(cmd+msg_delimiter+arg);
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
            sb.append(msg_delimiter);
            sb.append(arg);
        }
        outStream.println(sb.toString());
    }
    
    public Player getPlayer() {
        return player;
    }
    
    private void processMessage(String s) {
        String[] cmd_and_args = s.split(msg_delimiter_s, 2);
        
        String cmd = cmd_and_args[0];
        String[] args = null;
        
        if (cmd_and_args.length == 1) {
            args = new String[0];
        } else {
            args = cmd_and_args[1].split(msg_delimiter_s);
        }
        
        player.processMessage(cmd, args);
    }
    
    public void disconnect(DisconnectReason reason) {
        if (endpoint.isClosed()) return;
        
        if (player.isRegistered()) {
            Game.currentGame.getLogic().playerLeaves(player, reason);
            System.out.println(player.getName()+" disconnected ("+DisconnectReason.reasonNames[reason.ordinal()]+")");
        } else {
            System.out.println("An unregistered player disconnected ("+DisconnectReason.reasonNames[reason.ordinal()]+")");
        }
        Game.currentGame.getNetwork().disconnection(this);
        try {endpoint.close();}catch(IOException e){/* no idea what to do if close fails */}
        running = false;
    }
    
    public void run() {
        send("msg", "Hello");
        try {
            while (running) {
                String line = inStream.readLine();
                if (line == null) {
                    break;
                }
                processMessage(line);
            }
            disconnect(DisconnectReason.READ_ERROR);
        }
        catch (SocketTimeoutException e) {
            disconnect(DisconnectReason.TIMEOUT);
        }
        catch (IOException e) {
            disconnect(DisconnectReason.READ_ERROR);
        }
    }
}