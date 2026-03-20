package server;

import java.io.DataInputStream;
import java.io.IOException;

public class PacketHandler {
    private final ClientHandler client;
    private final GameServer gameServer;

    PacketHandler(ClientHandler client, GameServer gameServer) {
        this.gameServer = gameServer;
        this.client = client;
    }

    private void handleTestPacket(DataInputStream data) throws IOException {
        System.out.println("Received test packet with data: " + new String(data.readAllBytes()));
        GamePacket response = new GamePacket(PacketType.CLIENT_TEST, "Hello from the server!".getBytes());
        client.send(response);
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
                case CLIENT_TEST:
                    handleTestPacket(data);
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
