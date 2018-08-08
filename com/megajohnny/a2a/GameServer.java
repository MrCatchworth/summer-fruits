package com.megajohnny.a2a;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.net.ServerSocket;

public class GameServer {
    private static Card[] readDeck(String filename, CardColour colour) throws IOException, IndexOutOfBoundsException {
        BufferedReader deckFile = new BufferedReader(new FileReader(filename));
        ArrayList<Card> cardList = new ArrayList<Card>();
            
        String line;
        while ((line = deckFile.readLine()) != null) {
            if (line.isEmpty() || line.charAt(0) == '#') continue;
            Card c = Card.parse(line, colour);
            if (c == null) {
                throw new IndexOutOfBoundsException();
            } else {
                cardList.add(c);
            }
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
            return;
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println("Malformed green deck file");
            return;
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
            return;
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println("Malformed red deck file");
            return;
        }
        
        //make the game object and start listening for connections
        Game game = new Game(redDeck, greenDeck);
        try {
            ServerSocket listener = new ServerSocket(4746);
            while (true) {
                game.getNetwork().newConnection(listener.accept());
            }
        }
        catch(IOException e) {
            System.out.println("Oh Bother!");
            return;
        }
    }
}