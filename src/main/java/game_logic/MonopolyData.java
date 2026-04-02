package game_logic;

import game_logic.NonOwnableSquare.GoSquare;
import game_logic.OwnableSquare.Street;

import java.io.*;
import java.util.List;

public class MonopolyData implements Serializable {
    public List<Player> players;
    public int currentPlayer;
    public List<Square> squares;
    public int[] dice = new int[2];

    public MonopolyData(List<Player> players) {
        this.players = players;
        this.currentPlayer = 0;
        this.squares = List.of(
                new GoSquare(),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null),
                new Street(100, null)
        );
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public byte[] serialize() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try (ObjectOutputStream o = new ObjectOutputStream(os)) {
            o.writeObject(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return os.toByteArray();
    }

    public void deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        try (ObjectInputStream o = new ObjectInputStream(is)) {
            MonopolyData m = (MonopolyData) o.readObject();
            this.players = m.players;
            this.currentPlayer = m.currentPlayer;
            this.squares = m.squares;
            this.dice = m.dice;
            // TODO: add all other fields
        }
    }
}