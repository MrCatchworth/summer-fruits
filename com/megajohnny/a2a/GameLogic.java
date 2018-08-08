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
    private int curJudgeIndex;
    
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
        curJudgeIndex = 0;
    }
    
    public List<Player> getPlayers() {
        return players;
    }
    
    private void fillPlayersHand(Player p) {
        int i = p.getHandSize();
        while (p.getHandSize() < handSize) {
            p.deal(redDeck.draw());
        }
    }
    
    private void fillAllPlayersHands() {
        boolean nothingDone;
        do {
            nothingDone = true;
            for (Player p : players) {
                if (p.getHandSize() < handSize) {
                    p.deal(redDeck.draw());
                    nothingDone = false;
                }
            }
        } while (!nothingDone);
    }
    
    
    private void newRound(boolean reset) {
        if (reset) {
            //shuffle submissions into red deck
            redDeck.addAll(submissions);
            submissions.clear();
            redDeck.shuffle();
        }
        //draw green card
        if (!reset) curSubject = greenDeck.draw();
        //assign new judge
        if (reset) {
            curJudgeIndex = curJudgeIndex % players.size();
            curJudge = players.get(curJudgeIndex);
            nextJudgeIndex = (curJudgeIndex + 1) % players.size();
        } else {
            curJudge = players.get(nextJudgeIndex);
            curJudgeIndex = nextJudgeIndex;
            nextJudgeIndex = (nextJudgeIndex+1) % players.size();
        }
        
        //fill hands
        fillAllPlayersHands();
        System.out.println("Finished dealing");
        
        for (Player p : players) {
            p.send("msg", "A new round is starting");
            p.send("judge", String.valueOf(curJudge.getId()));
            p.send("subject", curSubject.toString());
        }
        
        changeState(State.SUBMISSION);
        
        for (Player p : players) {
            p.clearSubmission();
        }
        
        System.out.println("The judge is "+curJudge.getName());
    }
    
    private void sendState(Player joiner) {
        for (Player p : players) {
            sendPlayer(p, joiner, false);
        }
        joiner.send("state", String.valueOf(curState.ordinal()));
        
        if (curState == State.WAITING_FOR_PLAYERS || curState == State.END) return;
        
        joiner.send("judge", String.valueOf(curJudge.getId()));
        joiner.send("subject", String.valueOf(curSubject.toString()));
        
        if (curState == State.JUDGING) {
            for (Card c : submissions) {
                joiner.send("submission", c.toString());
            }
        } else {
            joiner.send("submcount", String.valueOf(submissions.size()));
        }
    }
    
    private void checkJudging() {
        System.out.print("Checking judging... ");
        for (Player p : players) {
            if (!p.hasSubmitted() && p != curJudge) {
                System.out.println("Not yet, "+p.getName()+" still needs to submit");
                return;
            }
        }
        System.out.println("Yes!");
        
        changeState(State.JUDGING);
        for (Player p : players) {
            for (Card c : submissions) {
                p.send("submission", c.toString());
            }
        }
    }
    
    public synchronized void judgeSelects(Player sendingPlayer, int id) {
        if (curState != State.JUDGING || curJudge != sendingPlayer) {
            //erroneous message; ignore?
            return;
        }
        
        if (id < 0 || id >= submissions.size()) return;
        Card c = submissions.get(id);
        
        if (!c.isValidSubmission()) {
            //tell judge sorry, but that player left
            curJudge.getNetwork().send("msg", "Sorry, the submitter of that card left, please choose another");
            return;
        }
        
        //award green card to owner of selection
        Player winner = c.getSubmitter();
        winner.setScore(winner.getScore()+1);
        
        //discard submissions (they need to be shuffled back in if the game ends)
        redDiscards.addAll(submissions);
        submissions.clear();
        
        //discard subject
        greenDiscards.add(curSubject);
        
        if (winner.getScore() >= scoreLimit) {
            //end the game
        } else {
            newRound(false);
        }
    }
    
    public synchronized void playerSubmits(Player sendingPlayer, Card submission) {
        System.out.println(sendingPlayer.getName()+" trying to submit "+submission.getName());
        
        if (curState != State.SUBMISSION || curJudge == sendingPlayer) {
            //erroneous message; ignore?
            return;
        }
        submissions.add(submission);
        
        for (Player p : Game.currentGame.getLogic().getPlayers()) {
            if (p != sendingPlayer) p.send("submitother", String.valueOf(sendingPlayer.getId()));
        }
        
        //if all players have submitted start the judging
        checkJudging();
    }
    
    public synchronized void playerJoins(Player joiner) {
        for (Player p : players) {
            sendPlayer(joiner, p, true);
        }
        
        sendState(joiner);
        
        players.add(joiner);
        
        //send current game state to joining player
        switch (curState) {
            case WAITING_FOR_PLAYERS:
                if (players.size() >= minPlayersToPlay) {
                    for (Player p : players) {
                        p.send("msg", "We have enough players to play!");
                    }
                    newRound(false);
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
    
    public synchronized void playerLeaves(Player leaver, DisconnectReason reason) {
        players.remove(leaver);
        
        //send message to remaining players
        for (Player p : players) {
            p.send("leave", String.valueOf(leaver.getId()), String.valueOf(reason.ordinal()));
        }
        
        switch(curState) {
            case WAITING_FOR_PLAYERS:
                //do nothing?
            break;
            
            case SUBMISSION:
                if (curJudge == leaver) {
                    //assign new judge
                    curJudgeIndex = curJudgeIndex % players.size();
                    curJudge = players.get(curJudgeIndex);
                    nextJudgeIndex = (curJudgeIndex + 1) % players.size();
                    for (Player p : players) {
                        p.send("judge", String.valueOf(curJudge.getId()));
                    }
                }
                //shuffle leaver's hand into red deck
                redDeck.addAll(leaver.getHand());
                leaver.getHand().clear();
                if (players.size() < minPlayersToPlay) {
                    cancelGame();
                } else {
                    checkJudging();
                }
            break;
            
            case JUDGING:
                if (curJudge == leaver) {
                    for (Player p : players) {
                        p.send("msg", "The judge has left, restarting round");
                    }
                    newRound(true);
                } else {
                    //mark leaver's submission as invalid
                    leaver.getSubmission().clearSubmission();
                }
                //shuffle player's hand into red deck
                redDeck.addAll(leaver.getHand());
                leaver.getHand().clear();
                
                if (players.size() < minPlayersToPlay) {
                    cancelGame();
                }
            break;
            
            case END:
                //do nothing?
            break;
        }
    }
    
    private void cancelGame() {
        System.out.println("The game is being cancelled");
        //notify players
        for (Player p : players) {
            p.send("reset");
        }
        curState = State.WAITING_FOR_PLAYERS;
        
        //now re-assemble the decks and shuffle
        
        //put hands back
        for (Player p : players) {
            redDeck.addAll(p.getHand());
            p.getHand().clear();
        }
        //put discards back
        redDeck.addAll(redDiscards);
        greenDeck.addAll(greenDiscards);
        redDiscards.clear();
        greenDiscards.clear();
        //put subject back
        greenDeck.add(curSubject);
        curSubject = null;
        //put submissions back
        for (Card c : submissions) {
            c.clearSubmission();
        }
        redDeck.addAll(submissions);
        submissions.clear();
        
        //shuffle re-assembled decks
        redDeck.shuffle();
        greenDeck.shuffle();
    }
    
    private void changeState(State newState) {
        curState = newState;
        for (Player p : players) {
            p.send("state", String.valueOf(curState.ordinal()));
        }
    }
    
    public synchronized void playerChats(Player chatter, String msg) {
        for (Player p : players) {
            if (p != chatter) p.send("chat", String.valueOf(chatter.getId()), msg);
        }
    }
    
    private void sendPlayer(Player p, Player recipient, boolean isJoiner) {
        recipient.send("player", String.valueOf(p.getId()), p.getName(), String.valueOf(p.getScore()), String.valueOf(p.getHandSize()), String.valueOf(p.hasSubmitted()), String.valueOf(isJoiner));
    }
}