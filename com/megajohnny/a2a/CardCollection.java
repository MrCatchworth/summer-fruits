package com.megajohnny.a2a;

import java.util.ArrayList;

//used for hands and decks - has a couple of functions for shuffling
public class CardCollection extends ArrayList<Card> {

    //shuffle: loop through and swap every index with a random other index
    public void shuffle() {
        int max = size()-1;
        
        for (int i=0; i<size(); i++) {
            int swapid = (int)(Math.random()*(max+1));
            Card temp = get(swapid);
            set(swapid, get(i));
            set(i, temp);
        }
    }
    
    //insert a card at a random position
    public void shuffleIn(Card c) {
        int insid = (int)(Math.random()*(size()));
        add(insid, c);
    }
    
    //like popping off the top of a stack (of cards)
    public Card draw() {
        return remove(size()-1);
    }
}