package com.megajohnny.a2a;

public class Game {
    private final GameNetwork network;
    private final GameLogic logic;
    
    public static Game currentGame;
    
    public Game() {
        currentGame = this;
        network = new GameNetwork(this);
        logic = new GameLogic(this);
    }
    
    public GameNetwork getNetwork() {
        return network;
    }
    public GameLogic getLogic() {
        return logic;
    }
}