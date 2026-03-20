package server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class GameState {

    private final GameServer server;

    private int currentPlayer = 0;

    // TODO: private Player[] players;

    /**
     * Constructor for client-side GameState
     */
    public GameState() {
        this.server = null;
    }

    /**
     * Constructor for server-side GameState
     */
    protected GameState(GameServer server) {
        this.server = server;
    }

    protected void rollDiceAndMove() {
        // TODO: Dice rolling logic, move player
        sendGameStateToClients();
    }

    /**
     * Broadcast current game state to all clients. Should be called whenever the game state changes.
     */
    protected void sendGameStateToClients() {
        try {
            GamePacket packet = new GamePacket(PacketType.SERVER_GAME_STATE_UPDATE, serialize());
            server.sendToAllClients(packet);
        } catch (Exception e) {
            System.out.println("Error sending game state to clients: " + e.getMessage());
        }
    }

    public byte[] serialize() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(os);
        try {
            out.writeInt(currentPlayer);
            // TODO...
            out.flush();
        } catch (Exception e) {
            System.out.println("Error serializing game state: " + e.getMessage());
        }
        return os.toByteArray();
    }

    public void deserialize(byte[] data) {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
        try {
            this.currentPlayer = in.readInt();
            // TODO...
        } catch (Exception e) {
            System.out.println("Error deserializing game state: " + e.getMessage());
        }
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }
}
