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
    
    private final String name;
    private final String desc;
    private final CardColour colour;
    
    private boolean validSubmission;
    private Player submitter;
    
    public Card(String name, String desc, CardColour colour) {
        this.name = name;
        this.desc = desc;
        this.colour = colour;
        validSubmission = false;
        submitter = null;
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
    
    public void setSubmission(Player p) {
        submitter = p;
        validSubmission = true;
    }
    public void clearSubmission() {
        submitter = null;
        validSubmission = false;
    }
    public boolean isValidSubmission() {
        return validSubmission;
    }
    public Player getSubmitter() {
        return submitter;
    }
    
    public String toString() {
        return name+":"+desc;
    }
}