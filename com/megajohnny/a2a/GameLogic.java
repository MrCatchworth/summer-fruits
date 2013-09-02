package com.megajohnny.a2a;

public class GameLogic {
    public enum State {WAITING_FOR_PLAYERS, SUBMISSION, JUDGING, END;
                       public static final String[] = {"Waiting for players", "Waiting for submissions", "Waiting for judge", "Post-game"};
                      };
    
    public static final int handSize = 7;
    public static final int minPlayersToPlay = 3;
    public static final int scoreLimit = 12;
    
    private ArrayList<Player> players;
    private State curState;
    private Player curJudge;
    private CardCollection submissions;
    
    
    private void newRound() {
        //draw green card
        //assign new judge
        //deal red cards until all players have 7
        curState = SUBMISSION;
    }
    
    public void judgeSelects(Player sendingPlayer, int id) {
        if (curState != JUDGING || curJudge != sendingPlayer) {
            //erroneous message; ignore?
            return;
        }
        
        //convert 'id' to card object
        if (!c.isValidSubmission()) {
            //tell judge sorry, but that player left
            return;
        }
        
        //award green card to owner of selection
        if (winner.score() >= scoreLimit) {
            //end the game
        } else {
            newRound();
        }
    }
    
    public void playerSubmits(Player sendingPlayer, int id) {
        if (curState != SUBMISSION || curJudge == sendingPlayer || sendingPlayer.hasSubmitted()) {
            //erroneous message; ignore?
            return;
        }
        //do stuff
    }
    
    public void playerJoins(Player joiner) {
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
    
    public void playerLeaves(Player leaver) {
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