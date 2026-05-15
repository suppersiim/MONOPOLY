package server;

import common.GamePacket;
import common.PacketType;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.Street;
import game_logic.Player;
import game_logic.Square;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PacketHandler {
    private final ClientHandler client;
    private final GameServer gameServer;

    PacketHandler(ClientHandler client, GameServer gameServer) {
        this.gameServer = gameServer;
        this.client = client;
    }

    private void handleClientJoinPacket(GamePacket packet) {
        String playerName = packet.getStringData().trim();
        GameManager gameManager = gameServer.getGameManager();
        if (gameManager.getGame() != null) {
            System.err.println("Player " + playerName + " tried to join, but game is already running!");
            return;
        }
        if (playerName.isBlank() || playerName.length() > 30 || gameManager.isPlayerJoined(playerName)) {
            System.err.println("Player " + playerName + " tried to join with invalid name!");
            return;
        }
        gameManager.addPlayer(playerName);
        int playerCount = gameManager.getJoinedPlayersCount();
        try {
            gameServer.sendToAllClients(new GamePacket(PacketType.SERVER_JOINED_PLAYERS_COUNT, Integer.toString(playerCount)));
        } catch (IOException e) {
            System.out.println("Error sending joined players count: " + e.getMessage());
        }
    }

    private void handleStartGamePacket() {
        gameServer.getGameManager().startGame();
    }

    private void handleRollPacket() {
        Monopoly monopoly = gameServer.getGameManager().getGame();
        if (monopoly == null) {
            System.out.println("No game state available; cannot handle roll packet.");
            return;
        }
        monopoly.onTurn();

        //If onTurn() paused for a buy decision, send an offer to the current player only
        if (monopoly.isWaitingForBuyResponse()) {
            String name = monopoly.getPendingPurchase().getName();
            int price = monopoly.getPendingPurchase().getPrice();
            String payload = name + ":" + price;
            try {
                client.send(new GamePacket(PacketType.SERVER_BUY_OFFER, payload));
            } catch (IOException e) {
                System.out.println("Error sending buy offer: " + e.getMessage());
            }
        }



        gameServer.getGameManager().broadcastGameState();
    }

    private void handleBuyResponsePacket(GamePacket packet) {
        Monopoly monopoly = gameServer.getGameManager().getGame();
        if (monopoly == null || !monopoly.isWaitingForBuyResponse()) {
            System.out.println("Received unexpected buy response; ignoring.");
            return;
        }

        String playerName = monopoly.getCurrentPlayer().getName();
        String propertyName = monopoly.getPendingPurchase().getName();
        int price = monopoly.getPendingPurchase().getPrice();

        String response = packet.getStringData().trim();
        boolean accepted = response.equalsIgnoreCase("yes");
        monopoly.resolveBuy(accepted);

        String buyMsg = accepted
                ? playerName + " bought " + propertyName + " for $" + price
                : playerName + " passed on " + propertyName;
        gameServer.getGameManager().broadcastEvent(buyMsg);
        gameServer.getGameManager().broadcastGameState();
    }

    private void handleBuyHouseResponsePacket(GamePacket packet) {
        System.out.println("handleBuyHouseResponsePacket called");
        Monopoly monopoly = gameServer.getGameManager().getGame();
        if (monopoly == null) {
            System.out.println("Received unexpected buy house response; ignoring.");
            return;
        }

        String payload = packet.getStringData().trim();
        boolean accepted = payload.startsWith("yes");

        if (!accepted) return;

        String streetName = payload.substring(4); // "yes:StreetName"
        Street street = monopoly.findStreetByName(streetName);
        if (street == null) {
            System.out.println("Street not found: " + streetName);
            return;
        }

        monopoly.setPendingPurchase(street);
        monopoly.resolveBuyHouse(true);

        String playerName = monopoly.getCurrentPlayer().getName();
        String buyMsg = playerName + " bought a house on " + streetName + " for $" + street.getHousePrice();
        gameServer.getGameManager().broadcastEvent(buyMsg);
        gameServer.getGameManager().broadcastGameState();
    }

    public void handleMortgagePacket(GamePacket packet) {
        Monopoly monopoly = gameServer.getGameManager().getGame();
        if (monopoly == null) return;

        String propertyName = packet.getStringData().trim();
        Player player = monopoly.getCurrentPlayer();

        game_logic.OwnableSquare.OwnableSquare target = null;
        for (game_logic.OwnableSquare.OwnableSquare p : player.getProperties()) {
            if (p.getName().equals(propertyName)) { target = p; break; }
        }
        if (target == null) return;

        if (!target.isMortgaged()) {
            player.mortgage(target);
            String event = player.getName() + " mortgaged " + propertyName + " for $" + target.getMortgageValue();
            gameServer.getGameManager().broadcastEvent(event);
        }
        gameServer.getGameManager().broadcastGameState();
    }

    private void handleTradeOfferPacket(GamePacket packet) {
        byte[] data = packet.getData();
        try (DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data))) {
            String toPlayerName = dis.readUTF();

            // get player by name
            Player toPlayer = gameServer.getGameManager().getGame().getPlayerByName(toPlayerName);
            if (toPlayer == null) {
                System.out.println("Trade offer to unknown player: " + toPlayerName);
                return;
            }

            // verify offer data
            int offeredMoney = dis.readInt();
            int offeredPropertiesCount = dis.readInt();
            List<OwnableSquare> offeredProperties = new ArrayList<>();
            int[] offeredPropertySquares = new int[offeredPropertiesCount];
            for (int i = 0; i < offeredPropertiesCount; i++) {
                int squareIndex = dis.readInt();
                Square square = gameServer.getGameManager().getGame().getSquare(squareIndex);
                if (!(square instanceof OwnableSquare)) {
                    System.out.println("Offered property is not ownable: " + square.getName());
                    return;
                }
                offeredPropertySquares[i] = squareIndex;
                offeredProperties.add((OwnableSquare) square);
            }
            int requestedMoney = dis.readInt();
            int requestedPropertiesCount = dis.readInt();
            List<OwnableSquare> requestedProperties = new ArrayList<>();
            int[] requestedPropertySquares = new int[requestedPropertiesCount];
            for (int i = 0; i < requestedPropertiesCount; i++) {
                int squareIndex = dis.readInt();
                Square square = gameServer.getGameManager().getGame().getSquare(squareIndex);
                if (!(square instanceof OwnableSquare)) {
                    System.out.println("Requested property is not ownable: " + square.getName());
                    return;
                }
                requestedPropertySquares[i] = squareIndex;
                requestedProperties.add((OwnableSquare) square);
            }

            // create unique ID for offer, stored in GameManager
            long tradeUID = gameServer.getGameManager().registerPendingTrade(
                gameServer.getGameManager().getGame().getCurrentPlayer().getName(),
                toPlayerName,
                offeredMoney,
                requestedMoney,
                offeredProperties,
                requestedProperties
            );

            // tradeUID is -1 if a pending trade already exists between these players (should not happen)
            if (tradeUID < 0) {
                System.out.println("Failed to register trade offer");
                return;
            }

            // send the offer packet to target player, including the tradeUID for response/identification
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (DataOutputStream dos = new DataOutputStream(baos)) {
                dos.writeLong(tradeUID);
                dos.writeInt(offeredMoney);
                dos.writeInt(offeredPropertiesCount);
                for (int squareIndex : offeredPropertySquares) {
                    dos.writeInt(squareIndex);
                }
                dos.writeInt(requestedMoney);
                dos.writeInt(requestedPropertiesCount);
                for (int squareIndex : requestedPropertySquares) {
                    dos.writeInt(squareIndex);
                }
                dos.flush();
                gameServer.sendPacketToPlayerByName(toPlayerName, new GamePacket(PacketType.SERVER_TRADE_OFFER, baos.toByteArray()));
            }
        } catch (IOException e) {
            System.out.println("Error handling trade offer packet: " + e.getMessage());
        }
    }

    public void handleUnmortgagePacket(GamePacket packet) {
        Monopoly monopoly = gameServer.getGameManager().getGame();
        if (monopoly == null) return;

        String propertyName = packet.getStringData().trim();
        Player player = monopoly.getCurrentPlayer();

        OwnableSquare target = null;
        for (OwnableSquare p : player.getProperties()) {
            if (p.getName().equals(propertyName)) { target = p; break; }
        }
        if (target == null || !target.isMortgaged()) return;

        int cost = target.getUnmortgageCost();
        if (player.getMoney() >= cost) {
            player.unmortgage(target);
            String event = player.getName() + " unmortgaged " + propertyName + " for $" + cost;
            gameServer.getGameManager().broadcastEvent(event);
        } else {
            gameServer.getGameManager().broadcastEvent(player.getName() + " cannot afford to unmortgage " + propertyName);
        }
        gameServer.getGameManager().broadcastGameState();
    }

    private void handleTradeResponsePacket(GamePacket packet) {
        String payload = packet.getStringData();
        String[] parts = payload.split(":");
        if (parts.length != 2) {
            System.out.println("Invalid trade response format");
            return;
        }

        boolean accepted = "accepted".equals(parts[0]);
        long tradeUID;
        try {
            tradeUID = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid trade UID in response");
            return;
        }

        gameServer.getGameManager().executeTrade(tradeUID, accepted);
    }

    private void handleQuitPacket() {
        System.out.println("Received quit packet. Closing connection.");
        try {
            client.close();
        } catch (IOException e) {
            System.out.println("Error closing client: " + e.getMessage());
        }
    }

    private void handleFinishTurnPacket() {
        if (!gameServer.getGameManager().getGame().getCurrentPlayer().hasRolled()) {
            System.out.println("Player tried to finish turn without rolling; ignoring.");
            return;
        }
        gameServer.getGameManager().getGame().advanceTurn();
        gameServer.getGameManager().broadcastGameState();
    }

    /**
     * Handle a packet from the client and dispatch to GameState
     * @param packet the packet to handle
     */
    public void handlePacket(GamePacket packet) {
        switch (packet.getType()) {
            case PacketType.CLIENT_JOIN:
                handleClientJoinPacket(packet);
                break;
            case CLIENT_START_GAME:
                handleStartGamePacket();
                break;
            case CLIENT_ROLL:
                handleRollPacket();
                break;
            case CLIENT_BUY_RESPONSE:
                handleBuyResponsePacket(packet);
                break;
            case CLIENT_BUY_HOUSE_RESPONSE:
                handleBuyHouseResponsePacket(packet);
                break;
            case CLIENT_MORTGAGE:
                handleMortgagePacket(packet);
                break;
            case CLIENT_TRADE_OFFER:
                 handleTradeOfferPacket(packet);
                 break;
            case QUIT:
                handleQuitPacket();
                break;
            case CLIENT_UNMORTGAGE:
                handleUnmortgagePacket(packet);
                break;
            case CLIENT_TRADE_RESPONSE:
                handleTradeResponsePacket(packet);
                break;
            case CLIENT_FINISH_TURN:
                handleFinishTurnPacket();
                break;
            default:
                System.out.println("Received unknown packet type: " + packet.getType());
                break;
        }
    }
}
