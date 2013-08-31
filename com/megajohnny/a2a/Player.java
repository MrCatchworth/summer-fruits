package com.megajohnny.a2a;

public class Player {
    private String name;
    private int score;
    private CardCollection hand;
    
    private PlayerNetwork network;
    
    public Player(String name, PlayerNetwork network) {
        this.name = name;
        this.network = network;
        score = 0;
        hand = new CardCollection();
    }
    
    public void sendMessage(String s) {
        network.sendMessage(s);
    }
    
    public void deal(Card c) {
        network.cardDealt();
    }
    
}