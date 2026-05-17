package server;

import common.GamePacket;
import common.PacketType;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager {

    private final GameServer server;
    private Monopoly game = null;

    private List<String> joinedPlayers = new ArrayList<>();

    private static GameManager instance;

    private Map<Long, TradeOffer> pendingTrades = new HashMap<>();

    private record TradeOffer (
        Player offerer,
        Player receiver,
        int offerAmount,
        int receiveAmount,
        List<OwnableSquare> offerProperties,
        List<OwnableSquare> receiveProperties
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

    public void broadcastAuctionState() {
        broadcastGameState();
    }

    public void startGame() {
        if (joinedPlayers.isEmpty()) {
            System.out.println("Not enough players to start the game.");
            return;
        }
        System.out.println("Starting game with players: " + String.join(", ", joinedPlayers));
        game = new Monopoly(new ArrayList<>(joinedPlayers.stream().map(Player::new).toList()));
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

    public long registerPendingTrade(String offererPlayer, String receiverPlayer, int offerAmount, int receiveAmount, List<OwnableSquare> offerProperties, List<OwnableSquare> receiveProperties) {

        Player offerer = game.getPlayerByName(offererPlayer);
        Player receiver = game.getPlayerByName(receiverPlayer);

        // verify no existing pending trade between the same players
        for (TradeOffer offer : pendingTrades.values()) {
            if (offer.offerer == offerer && offer.receiver == receiver) {
                System.err.println("Pending trade already exists between " + offererPlayer + " and " + receiverPlayer);
                return -1;
            }
        }

        long tradeId = System.nanoTime();
        pendingTrades.put(tradeId, new TradeOffer(offerer, receiver, offerAmount, receiveAmount, offerProperties, receiveProperties));
        return tradeId;
    }

    public void executeTrade(long tradeId, boolean accepted) {
        TradeOffer offer = pendingTrades.get(tradeId);
        if (offer == null) {
            System.err.println("No pending trade found with ID: " + tradeId);
            return;
        }
        pendingTrades.remove(tradeId);
        server.sendPacketToPlayerByName(offer.offerer.getName(), new GamePacket(PacketType.SERVER_TRADE_RESPONSE, accepted ? "accepted" : "rejected"));
        if (!accepted) {
            broadcastEvent(offer.receiver.getName() + " rejected trade offer from " + offer.offerer.getName());
            return;
        }

        // transfer money
        offer.offerer.payMoney(offer.offerAmount);
        offer.receiver.addMoney(offer.offerAmount);
        offer.receiver.payMoney(offer.receiveAmount);
        offer.offerer.addMoney(offer.receiveAmount);

        // transfer properties
        for (OwnableSquare property : offer.offerProperties) {
            property.setOwner(offer.receiver);
        }
        for (OwnableSquare property : offer.receiveProperties) {
            property.setOwner(offer.offerer);
        }

        broadcastGameState();

        broadcastEvent(offer.receiver.getName() + " accepted trade offer from " + offer.offerer.getName());
    }
}
