package server;

import common.GamePacket;
import common.PacketType;
import game_logic.Monopoly;
import game_logic.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {

    private final GameServer server;
    private List<String> joinedPlayers = new ArrayList<>();

    private Monopoly gameState = null;

    protected GameManager(GameServer server) {
        this.server = server;
    }

    /**
     * Broadcast current game state to all clients. Should be called whenever the game state changes.
     */
    protected void broadcastGameState() {
        if (gameState == null) {
            System.out.println("Game state is not initialized; skipping broadcast.");
            return;
        }

        try {
            GamePacket packet = new GamePacket(PacketType.SERVER_GAME_STATE_UPDATE, gameState.serialize());
            server.sendToAllClients(packet);
        } catch (Exception e) {
            System.out.println("Error sending game state to clients: " + e.getMessage());
        }
    }

    public Monopoly getGameState() {
        return gameState;
    }

    public void addPlayer(String playerName) {
        joinedPlayers.add(playerName);
        System.out.println(playerName + " joined the game.");
    }

    public void startGame() {
        if (joinedPlayers.isEmpty()) { // TODO: min 2 players
            System.out.println("Not enough players to start the game.");
            return;
        }
        System.out.println("Starting game with players: " + String.join(", ", joinedPlayers));
        gameState = new Monopoly(joinedPlayers.stream().map(Player::new).toList());
        broadcastGameState();
    }
}
