package com.megajohnny.a2a;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class GameServer {
    private static Card[] readDeck(String filename, CardColour colour) throws IOException, IndexOutOfBoundsException{
        BufferedReader deckFile = new BufferedReader(new FileReader(filename));
        ArrayList<Card> cardList = new ArrayList<Card>();
            
        String line;
        while ((line = deckFile.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            int colonpos = line.indexOf(':');
            String name = line.substring(0, colonpos);
            String desc = line.substring(colonpos+1, line.length());
            cardList.add(new Card(name, desc, colour));
        }
        
        deckFile.close();
        
        return cardList.toArray(new Card[cardList.size()]);
    }
    
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: GameServer green-deck red-deck");
            return;
        }
        
        Card[] greenDeck = null;
        Card[] redDeck = null;
        try {
            greenDeck = readDeck(args[0], CardColour.GREEN);
            for (Card c : greenDeck) {
                System.out.println("Green card");
                System.out.println("- Name: "+c.getName());
                System.out.println("- Desc: "+c.getDesc());
            }
        }
        catch (IOException e) {
            System.out.println("Problem reading the green deck file");
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println("Malformed green deck file");
        }
        
        try {
            redDeck = readDeck(args[0], CardColour.RED);
            for (Card c : greenDeck) {
                System.out.println("Red card");
                System.out.println("- Name: "+c.getName());
                System.out.println("- Desc: "+c.getDesc());
            }
        }
        catch (IOException e) {
            System.out.println("Problem reading the red deck file");
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println("Malformed red deck file");
        }
    }
}