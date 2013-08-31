package com.megajohnny.a2a;

import java.util.ArrayList;

public class CardCollection extends ArrayList<Card> {
    public void shuffle() {
        int max = size()-1;
        
        for (int i=0; i<size(); i++) {
            int swapid = (int)(Math.random()*(max+1));
            Card temp = get(swapid);
            set(swapid, get(i));
            set(i, temp);
        }
    }
    
    public void shuffleIn(Card c) {
        int insid = (int)(Math.random()*(size()));
        add(insid, c);
    }
}