package com.megajohnny.a2a;

public class Player {
    private final String name;
    private int score;
    private Card submission;
    private final CardCollection hand;
    
    public Player(String name, PlayerNetwork network) {
        this.name = name;
        this.network = network;
        score = 0;
        submission = null;
        hand = new CardCollection();
    }
}