package com.megajohnny.a2a;

public class Card {

    public static Card parse(String line, CardColour colour) {
        try {
            int colonpos = line.indexOf(':');
            String name = line.substring(0, colonpos);
            String desc = line.substring(colonpos+1, line.length());
            return new Card(name, desc, colour);
        }
        catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
    
    String name;
    String desc;
    CardColour colour;
    boolean validSubmission;
    
    public Card(String name, String desc, CardColour colour) {
        this.name = name;
        this.desc = desc;
        this.colour = colour;
        validSubmission = false;
    }
    
    public String getName() {
        return name;
    }
    public String getDesc() {
        return desc;
    }
    public CardColour getColour() {
        return colour;
    }
    
    public String toString() {
        return name+":"+desc;
    }
}