package client.client;

import client.Game;
import common.GamePacket;
import common.PacketType;
import game_logic.GameState;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PacketHandler {
    private final Game game;

    private Consumer<GameState> onGameStateUpdate = null;
    private Consumer<Integer> onJoinedPlayersCount = null;

    private Consumer<String> onEventLog = null;

    private BiConsumer<String, Integer> onBuyOffer = null;

    public PacketHandler(Game game) {
        this.game = game;
    }

    private void handleGameStateUpdate(GamePacket packet) {
        System.out.println("Received game state update from server");
        try {
            game.getGameState().deserialize(packet.getData());
            if (onGameStateUpdate != null) {
                onGameStateUpdate.accept(game.getGameState());
            }
        } catch (Exception e) {
            System.out.println("Error deserializing game state: " + e.getMessage());
        }
    }

    private void handleJoinedPlayersUpdate(GamePacket packet) {
        try {
            int count = Integer.parseInt(new String(packet.getData()));
            if (onJoinedPlayersCount != null) {
                onJoinedPlayersCount.accept(count);
            }
        } catch (Exception e) {
            System.out.println("Error parsing joined players count: " + e.getMessage());
        }
    }

    public void setOnGameStateUpdate(Consumer<GameState> onGameStateUpdate) {
        this.onGameStateUpdate = onGameStateUpdate;
    }

    public void setOnJoinedPlayersCount(Consumer<Integer> onJoinedPlayersCount) {
        this.onJoinedPlayersCount = onJoinedPlayersCount;
    }

    private void handleBuyOffer(GamePacket packet) {
        // Payload format: "<propertyName>:<price>"
        String payload = new String(packet.getData());
        int sep = payload.lastIndexOf(':');
        if (sep < 0 || onBuyOffer == null) return;
        String name = payload.substring(0, sep);
        int price;
        try {
            price = Integer.parseInt(payload.substring(sep + 1));
        } catch (NumberFormatException e) {
            System.out.println("Invalid buy offer payload: " + payload);
            return;
        }
        onBuyOffer.accept(name, price);
    }

    public void setOnBuyOffer(BiConsumer<String, Integer> onBuyOffer) {
        this.onBuyOffer = onBuyOffer;
    }

    public void setOnEventLog(Consumer<String> onEventLog) {
        this.onEventLog = onEventLog;
    }

    private void handleEventLog(GamePacket packet) {
        String msg = new String(packet.getData());
        if (onEventLog != null) onEventLog.accept(msg);
    }


    public void handlePacket(GamePacket packet) {
        switch (packet.getType()) {
            case PacketType.SERVER_GAME_STATE_UPDATE:
                handleGameStateUpdate(packet);
                break;
            case SERVER_JOINED_PLAYERS_COUNT:
                handleJoinedPlayersUpdate(packet);
                break;
            case SERVER_BUY_OFFER:
                handleBuyOffer(packet);
                break;
            case SERVER_EVENT_LOG:
                handleEventLog(packet);
                break;
        }
    }
}
