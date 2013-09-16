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
    private int id;
    
    //names the command used to register
    private static final String registerCommand = "reg";
    
    //maps a command string onto the object that handles it
    private static Map<String,MessageHandler> handlers;
    
    //every player has an ascending id, this specifies the id given to the next player
    private static int nextId = 0;
    
    static {
        //this block is where we set up all the message handlers
        
        handlers = new HashMap<String,MessageHandler>();
        
        //reg: the connecting player is telling us his name so he can play
        handlers.put("reg", new MessageHandler() {
            public boolean processMessage(String[] args, Player p) {
                if (args.length != 1) return false;
                if (p.isRegistered()) return false;
                p.register(args[0]);
                System.out.println(p.getName()+" has registered");
                return true;
            }
        });
        
        handlers.put("quit", new MessageHandler() {
            public boolean processMessage(String[] args, Player p) {
                if (args.length != 0) return false;
                if (!p.isRegistered()) return false;
                p.leaveGame();
                return true;
            }
        });
        
        handlers.put("say", new MessageHandler() {
            public boolean processMessage(String[] args, Player p) {
                if (args.length != 1) return false;
                if (!p.isRegistered()) return false;
                Game.currentGame.getLogic().playerChats(p, args[0]);
                return true;
            }
        });
        
        //heartbeat: does nothing, but avoids the server treating you as timed out
        handlers.put("hb", new MessageHandler() {
            public boolean processMessage(String[] args, Player p) {
                if (args.length != 0) return false;
                return true;
            }
        });
        
        //submit: used by non-judge players to submit a card from the hand
        handlers.put("submit", new MessageHandler() {
            public boolean processMessage(String[] args, Player p) {
                if (args.length != 1) return false;
                if (!p.isRegistered()) return false;
                int cardIndex = -1;
                try {cardIndex = Integer.parseInt(args[0]);} catch(NumberFormatException e) {return false;}
                if (cardIndex < 0 || cardIndex >= p.getHandSize()) return false;
                p.submit(cardIndex);
                return true;
            }
        });
        
        //select: used by the judge to select submitted cards in the judging phase
        handlers.put("select", new MessageHandler() {
            public boolean processMessage(String[] args, Player p) {
                if (args.length != 1) return false;
                if (!p.isRegistered()) return false;
                int id = -1;
                try {id = Integer.parseInt(args[0]);} catch(NumberFormatException e) {return false;}
                Game.currentGame.getLogic().judgeSelects(p, id);
                return true;
            }
        });
    }
    
    public Player(PlayerNetwork network) {
        this.network = network;
        score = 0;
        submission = null;
        registered = false;
        hand = new CardCollection();
        id = nextId++;
        name = "noname (id:"+id")";
    }
    
    public void processMessage(String cmd, String[] args) {
        MessageHandler handler = handlers.get(cmd);
        if (handler == null) {
            System.out.println("Unknown command");
            network.send("cmdfailed");
            return;
        }
        if (!handler.processMessage(args, this)) {
            System.out.println("Message handler returned false");
            network.send("cmdfailed");
            return;
        }
        network.send("ack");
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
    public CardCollection getHand() {
        return hand;
    }
    public void deal(Card c) {
        hand.add(c);
        send("deal", c.toString());
        for (Player p : Game.currentGame.getLogic().getPlayers()) {
            if (p != this) p.send("dealother", String.valueOf(id));
        }
    }
    public void submit(int i) {
        if (submission != null) {
            System.out.println(name+" tried to submit but did already");
            return;
        }
        Card subm = hand.remove(i);
        subm.setSubmission(this);
        submission = subm;
        Game.currentGame.getLogic().playerSubmits(this, subm);
    }
    public void leaveGame() {
        network.disconnect(DisconnectReason.VOLUNTARY);
    }
    public boolean hasSubmitted() {
        return submission != null;
    }
    public Card getSubmission() {
        return submission;
    }
    public void clearSubmission() {
        submission = null;
    }
    
    public int getScore() {
        return score;
    }
    public void setScore(int i) {
        score = i;
        for (Player p : Game.currentGame.getLogic().getPlayers()) {
            p.send("score", String.valueOf(id), String.valueOf(score));
        }
    }
    public int getId() {
        return id;
    }
    public void send(String cmd, String... args) {
        network.send(cmd, args);
    }
}