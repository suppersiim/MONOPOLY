package client;

import game_logic.Monopoly;

public class Game {

    private static Game instance;

    private Monopoly gameState = new Monopoly(null);
    private final GameClient gameClient;
    private volatile boolean running = false;

    private Game(String host, int port) {
        this.gameClient = new GameClient(this, host, port);
    }

    public static Game createInstance(String host, int port) throws Exception {
        if (instance != null) {
            instance.disconnect();
        }
        instance = new Game(host, port);
        return instance;
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

    public Monopoly getGameState() {
        return gameState;
    }

    public static Game getInstance() {
        return instance;
    }
}
