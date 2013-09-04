package com.megajohnny.a2a;

import java.util.Map;
import java.util.HashMap;

//represents the logic of a player in the game and handles messages passed by the attached PlayerNetwork
//a player starts off as unregistered (name = null, registered = false) and the game logic doesn't know about him/her
//the 'reg' command is used here to let the other end specify his/her name and finish joining the game
public class Player {
    //basic player stuff
    private String name;
    private int score;
    
    //card stuff
    private Card submission;
    private final CardCollection hand;
    
    //implementation stuff
    private boolean registered;
    private PlayerNetwork network;
    
    //names the command used to register
    private static final String registerCommand = "reg";
    
    //maps a command string onto the object that handles it
    private static Map<String,MessageHandler> handlers;
    
    static {
        //this block is where we set up all the message handlers
        
        handlers = new HashMap<String,MessageHandler>();
        
        //reg: the connecting player is telling us his name so he can play
        handlers.put("reg", new MessageHandler() {
            public boolean processMessage(String[] args, Player p) {
                if (args.length != 1) return false;
                p.register(args[0]);
                p.getNetwork().send("Hello "+p.getName()+"!");
                return true;
            }
        });
        
        handlers.put("quit", new MessageHandler() {
            public boolean processMessage(String[] args, Player p) {
                if (args.length != 0) return false;
                p.getNetwork().send("Goodbye!");
                p.leaveGame();
                return true;
            }
        });
        
        handlers.put("say", new MessageHandler() {
            public boolean processMessage(String[] args, Player p) {
                if (args.length != 1) return false;
                Game.currentGame.getLogic().playerChats(p, args[0]);
                return true;
            }
        });
        
        /*
        handlers.put("submit", new MessageHandler() {
            public boolean processMessage(String[] args, Player p) {
                if (args.length != 1) return false;
                int cardIndex = -1;
                try {cardIndex = Integer.parseInt(args[0]);} catch(NumberFormatException e) {return false;}
                if (cardIndex < 0 || cardIndex >= p.getHandSize()) return false;
                p.submit(cardIndex);
                return true;
            }
        });
        */
    }
    
    public Player(PlayerNetwork network) {
        name = null;
        this.network = network;
        score = 0;
        submission = null;
        registered = false;
        hand = new CardCollection();
    }
    
    public void processMessage(String cmd, String[] args) {
        MessageHandler handler = handlers.get(cmd);
        if (handler == null || !handler.processMessage(args, this)) {
            network.send("msg", "I didn't understand that!");
        }
    }
    
    public void register(String newName) {
        if (registered) return;
        registered = true;
        name = newName;
        Game.currentGame.getLogic().playerJoins(this);
    }
    public String getName() {
        return name;
    }
    public PlayerNetwork getNetwork() {
        return network;
    }
    public boolean isRegistered() {
        return registered;
    }
    
    public int getHandSize() {
        return hand.size();
    }
    public void deal(Card c) {
        hand.add(c);
    }
    public void submit(int i) {
        Card submission = hand.remove(i);
        submission.setSubmission(this);
        Game.currentGame.getLogic().playerSubmits(this, submission);
    }
    public void leaveGame() {
        if (registered) Game.currentGame.getLogic().playerLeaves(this);
        network.disconnect();
    }
    public boolean hasSubmitted() {
        return submission != null;
    }
    
    public int getScore() {
        return score;
    }
    public void setScore(int i) {
        score = i;
    }
}