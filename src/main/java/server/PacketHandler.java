package server;

import common.GamePacket;
import common.PacketType;
import game_logic.Monopoly;

import java.io.DataInputStream;
import java.io.IOException;

public class PacketHandler {
    private final ClientHandler client;
    private final GameServer gameServer;

    PacketHandler(ClientHandler client, GameServer gameServer) {
        this.gameServer = gameServer;
        this.client = client;
    }

    private void handleClientJoinPacket(DataInputStream data) throws IOException {
        String playerName = new String(data.readAllBytes());
        gameServer.getGameManager().addPlayer(playerName);
    }

    private void handleStartGamePacket(DataInputStream data) throws IOException {
        gameServer.getGameManager().startGame();
    }

    private void handleRollPacket(DataInputStream data) throws IOException {
        Monopoly gameState = gameServer.getGameManager().getGameState();
        if (gameState == null) {
            System.out.println("No game state available; cannot handle roll packet.");
            return;
        }
        gameState.onTurn();
        gameServer.getGameManager().broadcastGameState();
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
                case QUIT:
                    handleQuitPacket(data);
                    break;
            }
        } catch (IOException e) {
            System.out.println("Error handling [" + packet.getType() + "] packet: " + e.getMessage());
        }
    }
}
