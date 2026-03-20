package common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class GameState {
    private int currentPlayer;

    public int getCurrentPlayer() {
        return currentPlayer;
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
}
