package client;

import common.GameState;
import common.GamePacket;
import common.PacketType;

import java.io.DataInputStream;

public class Game {
    private final GameState gameState;
    private final GameClient gameClient;
    private volatile boolean running = false;

    public Game(String host, int port) {
        this.gameClient = new GameClient(this, host, port);
        gameState = new GameState();
    }

    public void start() throws Exception {
        gameClient.connect();
        running = true;
        // TODO: UI ja muu kraam

        DataInputStream cmdIn = new DataInputStream(System.in);
        while (running) {
            String line = cmdIn.readLine();
            gameClient.send(new GamePacket(PacketType.CLIENT_TEST, line.getBytes()));
        }
    }

    public void stop() throws Exception {
        running = false;
        gameClient.disconnect();
        // TODO: cleanup
    }

    /**
     * Update the game and UI. Called after game state changed.
     */
    public void update() {
        System.out.println("dbg current player: " + gameState.getCurrentPlayer());
    }

    public GameClient getClient() {
        return gameClient;
    }

    public GameState getGameState() {
        return gameState;
    }
}
