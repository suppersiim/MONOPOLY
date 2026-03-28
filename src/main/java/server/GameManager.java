package server;

import common.GamePacket;
import common.GameState;
import common.PacketType;

public class GameManager {

    private final GameServer server;

    private GameState gameState;

    protected GameManager(GameServer server) {
        this.server = server;
        this.gameState = new GameState();
    }

    protected void rollDiceAndMove() {
        // TODO: Dice rolling logic, move player
        broadcastGameState();
    }

    /**
     * Broadcast current game state to all clients. Should be called whenever the game state changes.
     */
    protected void broadcastGameState() {
        try {
            GamePacket packet = new GamePacket(PacketType.SERVER_GAME_STATE_UPDATE, gameState.serialize());
            server.sendToAllClients(packet);
        } catch (Exception e) {
            System.out.println("Error sending game state to clients: " + e.getMessage());
        }
    }

    public GameState getGameState() {
        return gameState;
    }
}
