package com.megajohnny.a2a;

public class Game {
    public enum State {WAITING_FOR_PLAYERS, SUBMISSION, JUDGING, END};
    
    public static final int handSize = 7;
    public static final int minPlayersToPlay = 3;
    
    private ArrayList<Player> players;
    private State curState;
    private Player curJudge;
    
    public synchronized void playerJoins(Player joiner) {
        players.add(joiner);
        //send message to other players
        //send current game state to joining player
        switch (curState) {
            case WAITING_FOR_PLAYERS:
                if (players.size() >= minPlayersToPlay) {
                    //start the game
                }
            break;
            case SUBMISSION:
                //deal handSize red cards to the joiner
            break;
            case JUDGING:
                //deal handSize red cards to the joiner
            break;
            case END:
                //do nothing?
            break;
        }
    }
    
    public synchronized void playerLeaves(Player leaver) {
        players.remove(leaver);
        //send message to remaining players
        
        switch(curState) {
            case WAITING_FOR_PLAYERS:
                //do nothing?
            break;
            
            case SUBMISSION:
                if (curJudge == leaver) {
                    //assign new judge
                }
                //shuffle leaver's hand into red deck
                if (players.size() < minPlayersToPlay) {
                    //end the game, go to WAITING_FOR_PLAYERS
                }
            break;
            
            case JUDGING:
                if (curJudge == leaver) {
                    //shuffle submissions into red deck
                    //deal 1 red card to each remaining player
                    //assign new judge
                } else {
                    //discard leaver's submission
                    //send a message to the judge saying this
                }
                //shuffle player's hand into red deck
                if (players.size() < minPlayersToPlay) {
                    //end the game, go to WAITING_FOR_PLAYERS
                }
            break;
            
            case END:
                //do nothing?
            break;
        }
    }