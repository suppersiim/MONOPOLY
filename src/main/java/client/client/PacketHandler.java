package client.client;

import client.Game;
import client.gui.BoardView;
import common.GamePacket;
import common.PacketType;
import game_logic.GameState;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.Square;
import server.GameServer;
import server.Monopoly;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PacketHandler {
    private final Game game;

    private Consumer<GameState> onGameStateUpdate = null;
    private Consumer<Integer> onJoinedPlayersCount = null;

    private Consumer<String> onEventLog = null;
    private Consumer<String> onJailCardOffer = null;

    private BiConsumer<String, Integer> onBuyOffer = null;
    private BiConsumer<String, Integer> onBuyHouseOffer = null;
    private Consumer<BoardView.TradeInfo> onTradeOffer = null;
    private Consumer<Boolean> onTradeResponse = null;
    private Consumer<GameState> onAuctionUpdate = null;

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
            if (game.getGameState().auctionState != null && onAuctionUpdate != null) {
                onAuctionUpdate.accept(game.getGameState());
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
        String payload = packet.getStringData();
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

    public void setOnBuyHouseOffer(BiConsumer<String, Integer> onBuyHouseOffer) {
        this.onBuyHouseOffer = onBuyHouseOffer;
    }

    public void setOnTradeOffer(Consumer<BoardView.TradeInfo> onTradeOffer) {
        this.onTradeOffer = onTradeOffer;
    }

    public void setOnTradeResponse(Consumer<Boolean> onTradeResponse) {
        this.onTradeResponse = onTradeResponse;
    }

    private void handleBuyHouseOffer(GamePacket packet) {
        String payload = packet.getStringData();
        int sep = payload.lastIndexOf(':');
        if (sep < 0 || onBuyHouseOffer == null) return;
        String name = payload.substring(0, sep);
        int price;
        try {
            price = Integer.parseInt(payload.substring(sep + 1));
        } catch (NumberFormatException e) {
            System.out.println("Invalid buy house offer payload: " + payload);
            return;
        }
        onBuyHouseOffer.accept(name, price);
    }

    public void handleMortgagePacket(GamePacket packet){

    }

    public void setOnEventLog(Consumer<String> onEventLog) {
        this.onEventLog = onEventLog;
    }

    private void handleEventLog(GamePacket packet) {
        String msg = packet.getStringData();
        if (onEventLog != null) onEventLog.accept(msg);
    }

    public void setOnJailCardOffer(Consumer<String> onJailCardOffer) {
        this.onJailCardOffer = onJailCardOffer;
    }

    private void handleJailCardOffer(GamePacket packet) {
        if (onJailCardOffer != null) onJailCardOffer.accept(packet.getStringData());
    }

    public void handleTradeOffer(GamePacket packet) {
        byte[] data = packet.getData();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        try (DataInputStream dis = new DataInputStream(bais)) {

            long offerId = dis.readLong();

            int offerMoney = dis.readInt();
            int offeredCount = dis.readInt();
            String[] offeredProperties = new String[offeredCount];
            for (int i = 0; i < offeredCount; i++) {
                int squareIndex = dis.readInt();
                Square square = game.getGameState().getSquare(squareIndex);
                offeredProperties[i] = square.getName();
            }

            int requestMoney = dis.readInt();
            int requestedCount = dis.readInt();
            String[] requestedProperties = new String[requestedCount];
            for (int i = 0; i < requestedCount; i++) {
                int squareIndex = dis.readInt();
                Square square = game.getGameState().getSquare(squareIndex);
                requestedProperties[i] = square.getName();
            }

            String currentPlayerName = game.getGameState().getCurrentPlayer().getName();
            BoardView.TradeInfo tradeInfo = new BoardView.TradeInfo(offerId, currentPlayerName, offerMoney, requestMoney, offeredProperties, requestedProperties);
            if (onTradeOffer != null) {
                onTradeOffer.accept(tradeInfo);
            }
        } catch (Exception e) {
            System.out.println("Error parsing trade offer: " + e.getMessage());
        }
    }

    public void handleTradeResponse(GamePacket packet) {
        String data = packet.getStringData();
        if (onTradeResponse != null) {
            onTradeResponse.accept(data.equals("accepted"));
        }
    }

    public void setOnAuctionUpdate(Consumer<GameState> onAuctionUpdate) {
        this.onAuctionUpdate = onAuctionUpdate;
    }


    public void handlePacket(GamePacket packet) {
        System.out.println("Handling packet of type " + packet.getType());
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
            case SERVER_BUY_HOUSE_OFFER:
                handleBuyHouseOffer(packet);
                break;
            case SERVER_EVENT_LOG:
                handleEventLog(packet);
                break;
            case SERVER_TRADE_OFFER:
                handleTradeOffer(packet);
                break;
            case SERVER_TRADE_RESPONSE:
                handleTradeResponse(packet);
                break;
            case SERVER_USE_JAIL_CARD_OFFER:
                handleJailCardOffer(packet);
                break;
        }
    }
}
