package client;

import client.client.GameClient;
import game_logic.GameState;

public class Game {

    private static Game instance;

    private String playerName;
    private GameState gameState = new GameState(null);
    private final GameClient gameClient;
    private volatile boolean running = false;

    private Game(String host, int port, String playerName) {
        this.gameClient = new GameClient(this, host, port);
        this.playerName = playerName;
    }

    public static void createInstance(String host, int port, String playerName) throws Exception {
        if (instance != null) {
            instance.disconnect();
        }
        instance = new Game(host, port, playerName);
    }

    public void connect() throws Exception {
        gameClient.connect();
        running = true;
    }

    public void disconnect() throws Exception {
        running = false;
        gameClient.disconnect();
    }

    public GameClient getClient() {
        return gameClient;
    }

    public GameState getGameState() {
        return gameState;
    }

    public static Game getInstance() {
        return instance;
    }

    public String getPlayerName() {
        return playerName;
    }
}
