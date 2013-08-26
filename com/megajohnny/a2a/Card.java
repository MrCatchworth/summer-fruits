package com.megajohnny.a2a;

public class Card {
    
    String name;
    String desc;
    CardColour colour;
    
    public Card(String name, String desc, CardColour colour) {
        this.name = name;
        this.desc = desc;
        this.colour = colour;
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
}