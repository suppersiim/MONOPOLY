package server;

import common.GamePacket;
import common.PacketType;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.Street;
import game_logic.Player;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Properties;

public class PacketHandler {
    private final ClientHandler client;
    private final GameServer gameServer;

    PacketHandler(ClientHandler client, GameServer gameServer) {
        this.gameServer = gameServer;
        this.client = client;
    }

    private void handleClientJoinPacket(DataInputStream data) throws IOException {
        String playerName = new String(data.readAllBytes());
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
        gameServer.sendToAllClients(new GamePacket(PacketType.SERVER_JOINED_PLAYERS_COUNT, Integer.toString(playerCount)));
    }

    private void handleStartGamePacket(DataInputStream data) throws IOException {
        gameServer.getGameManager().startGame();
    }

    private void handleRollPacket(DataInputStream data) throws IOException {
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
            client.send(new GamePacket(PacketType.SERVER_BUY_OFFER, payload));
        }



        gameServer.getGameManager().broadcastGameState();
    }

    private void handleBuyResponsePacket(DataInputStream data) throws IOException {
        Monopoly monopoly = gameServer.getGameManager().getGame();
        if (monopoly == null || !monopoly.isWaitingForBuyResponse()) {
            System.out.println("Received unexpected buy response; ignoring.");
            return;
        }

        String playerName = monopoly.getCurrentPlayer().getName();
        String propertyName = monopoly.getPendingPurchase().getName();
        int price = monopoly.getPendingPurchase().getPrice();

        String response = new String(data.readAllBytes()).trim();
        boolean accepted = response.equalsIgnoreCase("yes");
        monopoly.resolveBuy(accepted);

        String buyMsg = accepted
                ? playerName + " bought " + propertyName + " for $" + price
                : playerName + " passed on " + propertyName;
        gameServer.getGameManager().broadcastEvent(buyMsg);
        gameServer.getGameManager().broadcastGameState();
    }

    private void handleBuyHouseResponsePacket(DataInputStream data) throws IOException {
        System.out.println("handleBuyHouseResponsePacket called");
        Monopoly monopoly = gameServer.getGameManager().getGame();
        if (monopoly == null) {
            System.out.println("Received unexpected buy house response; ignoring.");
            return;
        }

        String payload = new String(data.readAllBytes()).trim();
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

    public void handleMortgagePacket(DataInputStream data) throws IOException{
        return;
    }

    private void handleQuitPacket(DataInputStream data) throws IOException {
        System.out.println("Received quit packet. Closing connection.");
        client.close();
    }

    /**
     * Handle a packet from the client and dispatch to GameState
     * @param packet the packet to handle
     */
    public void handlePacket(GamePacket packet) {
        DataInputStream data = new DataInputStream(packet.getDataStream());
        try {
            switch (packet.getType()) {
                case PacketType.CLIENT_JOIN:
                    handleClientJoinPacket(data);
                    break;
                case CLIENT_START_GAME:
                    handleStartGamePacket(data);
                    break;
                case CLIENT_ROLL:
                    handleRollPacket(data);
                    break;
                case CLIENT_BUY_RESPONSE:
                    handleBuyResponsePacket(data);
                    break;
                case CLIENT_BUY_HOUSE_RESPONSE:
                    handleBuyHouseResponsePacket(data);
                    break;
                case CLIENT_MORTGAGE:
                    handleMortgagePacket(data);
                    break;
                case QUIT:
                    handleQuitPacket(data);
                    break;
            }
        } catch (IOException e) {
            System.out.println("Error handling [" + packet.getType() + "] packet: " + e.getMessage());
        }
    }
}
