package common;

import game_logic.Player;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

public class GameState {
    private int currentPlayer;
    private List<Player> players = new ArrayList<>();

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public byte[] serialize() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(os);
        try {
            out.writeInt(currentPlayer);
            // TODO...
            out.writeInt(players.size());
            for (Player p : players) {
                out.writeUTF(p.getName());
                out.writeInt(p.getMoney());
                out.writeInt(p.getLocation());
                out.writeBoolean(p.isInJail());
            }
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
            int playerCount = in.readInt();
            this.players = new ArrayList<>();
            for (int i = 0; i < playerCount; i++) {
                String name = in.readUTF();
                int money = in.readInt();
                int location = in.readInt();
                boolean inJail = in.readBoolean();
                Player p = new Player(name);
                this.players.add(p);
            }
        } catch (Exception e) {
            System.out.println("Error deserializing game state: " + e.getMessage());
        }
    }
}
