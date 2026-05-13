package server;

import common.GamePacket;
import common.PacketType;
import game_logic.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GameManager {

    private final GameServer server;
    private Monopoly game = null;

    private List<String> joinedPlayers = new ArrayList<>();

    private static GameManager instance;

    private Map<Long, TradeOffer> pendingTrades;

    private record TradeOffer (
        String offererPlayer,
        String receiverPlayer,
        int offerAmount,
        int receiveAmount,
        int[] offerProperties,
        int[] receiveProperties
    ) {}

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

    public long registerPendingTrade(String offererPlayer, String receiverPlayer, int offerAmount, int receiveAmount, int[] offerProperties, int[] receiveProperties) {

        // verify no existing pending trade between the same players
        for (TradeOffer offer : pendingTrades.values()) {
            if (offer.offererPlayer.equals(offererPlayer) && offer.receiverPlayer.equals(receiverPlayer)) {
                System.err.println("Pending trade already exists between " + offererPlayer + " and " + receiverPlayer);
                return -1;
            }
        }

        long tradeId = System.nanoTime();
        pendingTrades.put(tradeId, new TradeOffer(offererPlayer, receiverPlayer, offerAmount, receiveAmount, offerProperties, receiveProperties));
        return tradeId;
    }

    public void executeTrade(long tradeId) {
        TradeOffer offer = pendingTrades.get(tradeId);
        if (offer == null) {
            System.err.println("No pending trade found with ID: " + tradeId);
            return;
        }

        pendingTrades.remove(tradeId);
    }
}
