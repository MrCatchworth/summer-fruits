package com.megajohnny.a2a;

import java.util.List;
import java.util.ArrayList;

public class GameLogic {
    public enum State {WAITING_FOR_PLAYERS, SUBMISSION, JUDGING, END;
                       public static final String[] stateNames = {"Waiting for players", "Waiting for submissions", "Waiting for judge", "Post-game"};
                      };
    
    public static final int handSize = 7;
    public static final int minPlayersToPlay = 3;
    public static final int scoreLimit = 12;
    
    private List<Player> players;
    private State curState;
    private Player curJudge;
    private CardCollection submissions;
    private CardCollection redDeck;
    private CardCollection greenDeck;
    private CardCollection redDiscards;
    private CardCollection greenDiscards;
    private Card curSubject;
    private GameNetwork network;
    private Game game;
    private int nextJudgeIndex;
    
    public GameLogic (Game g, Card[] rdContents, Card[] gdContents) {
        game = g;
        redDeck = new CardCollection();
        for (Card c : rdContents) redDeck.add(c);
        greenDeck = new CardCollection();
        for (Card c : gdContents) greenDeck.add(c);
        redDeck.shuffle();
        greenDeck.shuffle();
        
        redDiscards = new CardCollection();
        greenDiscards = new CardCollection();
        curSubject = null;
        network = game.getNetwork();
        submissions = new CardCollection();
        players = new ArrayList<Player>();
        curState = State.WAITING_FOR_PLAYERS;
        curJudge = null;
        nextJudgeIndex = 0;
    }
    
    private void fillPlayersHand(Player p) {
        int i = p.getHandSize();
        while (p.getHandSize() < handSize) {
            p.deal(redDeck.draw());
        }
    }
    
    private void newRound() {
        //draw green card
        curSubject = greenDeck.draw();
        //assign new judge
        curJudge = players.get(nextJudgeIndex);
        nextJudgeIndex = (nextJudgeIndex+1) % players.size();
        
        for (Player p : players) {
            p.getNetwork().send("msg", "A new round is starting!");
            p.getNetwork().send("judge", curJudge.getName());
            
            p.getNetwork().send("msg", "The new subject is "+curSubject.getName());
            p.getNetwork().send("msg", "Players, submit your cards!");
        }
        
        //deal red cards until all players have 7
        curState = State.SUBMISSION;
    }
    
    public synchronized void judgeSelects(Player sendingPlayer, int id) {
        if (curState != State.JUDGING || curJudge != sendingPlayer) {
            //erroneous message; ignore?
            return;
        }
        
        Card c = submissions.get(id);
        
        if (!c.isValidSubmission()) {
            //tell judge sorry, but that player left
            return;
        }
        
        //award green card to owner of selection
        Player winner = c.getSubmitter();
        winner.setScore(winner.getScore()+1);
        if (winner.getScore() >= scoreLimit) {
            //end the game
        } else {
            newRound();
        }
    }
    
    public synchronized void playerSubmits(Player sendingPlayer, Card submission) {
        if (curState != State.SUBMISSION || curJudge == sendingPlayer || sendingPlayer.hasSubmitted()) {
            //erroneous message; ignore?
            return;
        }
        
    }
    
    public synchronized void playerJoins(Player joiner) {
        for (Player p : players) {
            p.getNetwork().send("msg", joiner.getName()+" has joined the game");
        }
        
        players.add(joiner);
        
        joiner.getNetwork().send("msg", "Welcome to the game, "+joiner.getName());
        joiner.getNetwork().send("msg", "The game is currently: "+State.stateNames[curState.ordinal()]);
        
        //send current game state to joining player
        switch (curState) {
            case WAITING_FOR_PLAYERS:
                if (players.size() >= minPlayersToPlay) {
                    for (Player p : players) {
                        p.getNetwork().send("msg", "We have enough players to play!");
                        newRound();
                    }
                }
            break;
            case SUBMISSION:
                fillPlayersHand(joiner);
            break;
            case JUDGING:
                fillPlayersHand(joiner);
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
                    //mark leaver's submission as invalid
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
        
    public synchronized void playerChats(Player chatter, String msg) {
        for (Player p : players) {
            if (p != chatter) p.getNetwork().send("chat", new String[] {chatter.getName(), msg});
        }
    }
}