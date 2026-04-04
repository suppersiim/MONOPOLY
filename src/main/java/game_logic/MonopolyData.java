package game_logic;

import game_logic.NonOwnableSquare.*;
import game_logic.OwnableSquare.RailRoad;
import game_logic.OwnableSquare.Street;
import game_logic.OwnableSquare.Utility;

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
                // 1. SECTOR:
                new GoSquare(), // 0
                new Street(2, null, "BROWN",1, "Mediterranean Avenue"), // 1
                new CommunityChest(), // 2
                new Street(4, null, "BROWN",1, "Baltic Avenue"), // 3
                new Tax(200), // 4
                new RailRoad(0,null,"Reading Railroad"), // 5
                new Street(6, null, "LIGHT_BLUE",1, "Oriental Avenue"), // 6
                new Chance(), // 7
                new Street(6, null, "LIGHT_BLUE",1, "Vermont Avenue"), // 8
                new Street(8, null, "LIGHT_BLUE",1, "Connecticut Avenue"), // 9
                new Jail(), // 10

                // 2. SECTOR:
                new Street(10, null, "PINK",2, "St. Charles Place"), // 11
                new Utility(0,null, "Electric Company"), // 12
                new Street(10, null, "PINK",2, "States Avenue"), // 13
                new Street(12, null, "PINK",2, "Virginia Avenue"), // 14
                new RailRoad(0,null,"Pennsylvania Railroad"), // 15
                new Street(14, null, "ORANGE",2, "St. James Place"), // 16
                new CommunityChest(), // 17
                new Street(14, null, "ORANGE",2, "Tennessee Avenue"), // 18
                new Street(16, null, "ORANGE",2, "New York Avenue"), // 19
                new FreeParking(), // 20

                // 3. SECTOR:
                new Street(18, null, "RED",3, "Kentucky Avenue"), // 21
                new Chance(), // 22
                new Street(18, null, "RED",3, "Indiana Avenue"), // 23
                new Street(20, null, "RED",3, "Illinois Avenue"), // 24
                new RailRoad(0,null,"B&O Railroad"), // 25
                new Street(22, null, "YELLOW",3, "Atlantic Avenue"), // 26
                new Street(22, null, "YELLOW",3, "Ventnor Avenue"), // 27
                new Utility(0,null,"Water Works"), // 28
                new Street(24, null, "YELLOW",3, "Marvin Gardens"), // 29
                new GoToJail(), // 30

                // 4. SECTOR:
                new Street(26, null, "GREEN",4, "Pacific Avenue"), // 31
                new Street(26, null, "GREEN",4, "North Carolina Avenue"), // 32
                new CommunityChest(), // 33
                new Street(28, null, "GREEN",4, "Pennsylvania Avenue"), // 34
                new RailRoad(0,null,"Short Line Railroad"), // 35
                new Chance(), // 36
                new Street(35, null, "DARK_BLUE",4, "Park Place"), // 37
                new Tax(100), // 38
                new Street(50, null, "DARK_BLUE",4, "Boardwalk") // 39
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