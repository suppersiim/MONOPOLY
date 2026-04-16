package server;

import common.GamePacket;
import common.PacketType;
import game_logic.Player;

import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private final GameServer server;
    private Monopoly game = null;

    private List<String> joinedPlayers = new ArrayList<>();

    private static GameManager instance;

    protected GameManager(GameServer server) {
        this.server = server;
        instance = this;
    }

    /**
     * Broadcast current game state to all clients. Should be called whenever the game state changes.
     */
    protected void broadcastGameState() {
        if (game == null) {
            System.out.println("Game state is not initialized; skipping broadcast.");
            return;
        }

        try {
            GamePacket packet = new GamePacket(PacketType.SERVER_GAME_STATE_UPDATE, game.serialize());
            server.sendToAllClients(packet);
        } catch (Exception e) {
            System.out.println("Error sending game state to clients: " + e.getMessage());
        }
    }

    public Monopoly getGame() {
        return game;
    }

    public void addPlayer(String playerName) {
        joinedPlayers.add(playerName);
        System.out.println(playerName + " joined the game.");
    }

    public boolean isPlayerJoined(String playerName) {
        return joinedPlayers.contains(playerName);
    }

    public int getJoinedPlayersCount() {
        return joinedPlayers.size();
    }

    public void broadcastEvent(String message) {
        try {
            GamePacket packet = new GamePacket(PacketType.SERVER_EVENT_LOG, message);
            server.sendToAllClients(packet);
        } catch (Exception e) {
            System.out.println("Error sending event: " + e.getMessage());
        }
    }

    public void startGame() {
        if (joinedPlayers.isEmpty()) { // TODO: min 2 players
            System.out.println("Not enough players to start the game.");
            return;
        }
        System.out.println("Starting game with players: " + String.join(", ", joinedPlayers));
        game = new Monopoly(joinedPlayers.stream().map(Player::new).toList());
        broadcastGameState();
    }

    public void resetGame() {
        game = null;
        joinedPlayers.clear();
        System.out.println("Game reset. Waiting for players to join...");
    }

    public GameServer getServer() {
        return server;
    }

    public static GameManager getInstance() {
        return instance;
    }
}
