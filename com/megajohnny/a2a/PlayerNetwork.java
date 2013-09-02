package com.megajohnny.a2a;

import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

//a class to handle all the networking worries, so the non-*Network classes don't have to
public class PlayerNetwork extends Thread {
    private final Socket endpoint;
    private final BufferedReader inStream;
    private final PrintWriter outStream;
    private Player player;
    private boolean running;
    
    //maps a command string onto the object that handles it
    private static Map<String,MessageHandler> handlers;
    
    static {
        //this block is where we set up all the message handlers
        
        handlers = new HashMap<String,MessageHandler>();
        
        //reg: the connecting player is telling us his name so he can play
        handlers.put("reg", new MessageHandler() {
            public boolean processMessage(String[] args, PlayerNetwork pn) {
                if (pn.getPlayer() != null) return false;
                if (args.length != 1) return false;
                pn.setPlayer(new Player(args[0], pn));
                pn.send("Hello "+args[0]+"!");
                return true;
            }
        });
        
        //quit: the player is disconnecting voluntarily/leaving the game
        handlers.put("quit", new MessageHandler() {
            public boolean processMessage(String[] args, PlayerNetwork pn) {
                pn.send("Goodbye!");
                pn.disconnect();
                return true;
            }
        });
    }
    
    public PlayerNetwork(Socket endpoint) throws IOException {
        this.endpoint = endpoint;
        inStream = new BufferedReader(new InputStreamReader(endpoint.getInputStream()));
        outStream = new PrintWriter(endpoint.getOutputStream(), true);
        player = null;
        running = true;
    }
    
    //send a command with one argument (to avoid mucking about with arrays and loops)
    public synchronized void send(String cmd, String arg) {
        outStream.println(cmd+"\t"+arg);
    }
    //send a command with no arguments
    public synchronized void send(String cmd) {
        outStream.println(cmd);
    }
    //send a command with some arguments
    public synchronized void send(String cmd, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append(cmd);
        for (String arg : args) {
            sb.append("\t");
            sb.append(arg);
        }
        outStream.println(sb.toString());
    }
    
    public synchronized Player getPlayer() {
        return player;
    }
    public synchronized void setPlayer(Player p) {
        player = p;
    }
    
    private synchronized void processMessage(String s) {
        String[] cmd_and_args = s.split("\t", 2);
        
        String cmd = cmd_and_args[0];
        String[] args = null;
        
        if (cmd_and_args.length == 1) {
            args = new String[0];
        } else {
            args = cmd_and_args[1].split("\t");
        }
        
        MessageHandler handler = handlers.get(cmd);
        if (handler == null || !handler.processMessage(args, this)) {
            send("msg", "I didn't understand that!");
        }
    }
    
    public synchronized void disconnect() {
        try {endpoint.close();}catch(IOException e){/* no idea what to do if close fails */}
        running = false;
    }
    
    public synchronized void readError() {
        disconnect();
    }
    
    public void run() {
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