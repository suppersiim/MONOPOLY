package server;

public class PacketHandler {
    private final ClientHandler client;
    private final GameServer gameServer;

    PacketHandler(ClientHandler client, GameServer gameServer) {
        this.gameServer = gameServer;
        this.client = client;
    }

    private void handleTestPacket(byte[] data) {
        System.out.println("Received test packet with data: " + new String(data));
        client.send(PacketType.TEST, "Hello from the server!".getBytes());
    }

    private void handleQuitPacket(byte[] data) {
        System.out.println("Received quit packet. Closing connection.");
        client.close();
    }

    /**
     * Handle a packet from the client and dispatch to GameState
     * @param packetType packet type
     * @param data payload of the packet (might be empty)
     */
    public void handlePacket(PacketType packetType, byte[] data) {
        switch (packetType) {
            case TEST:
                handleTestPacket(data);
                break;
            case QUIT:
                handleQuitPacket(data);
                break;
        }
    }
}
