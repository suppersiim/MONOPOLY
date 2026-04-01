package game_logic;

import game_logic.NonOwnableSquare.GoSquare;
import game_logic.OwnableSquare.Street;

import java.io.*;
import java.util.List;

public class Monopoly implements Serializable {
    private List<Player> players;
    private int currentPlayer;
    private List<Square> squares;
    private final int[] dice = new int[2];

    public Monopoly(List<Player> players) {
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

    public int[] diceRoll(){
        dice[0] = (int)(Math.random()*6+1);
        dice[1] = (int)(Math.random()*6+1);
        return dice;
    }

    public Square landedOn(int location){
        return squares.get(location);
    }

    public void onTurn(){
        if (!players.get(currentPlayer).isInJail()) {
            int doubles = 0;
            int[] dice = diceRoll();
            if (dice[0] == dice[1]) {
                doubles += 1;
                if (doubles == 3) {
                    players.get(currentPlayer).goJail();
                }
            }
            players.get(currentPlayer).move(dice[0] + dice[1]);

            Square squareCurrent = landedOn(players.get(currentPlayer).getLocation());
            System.out.println("Player " + players.get(currentPlayer).getName() + " rolled " + dice[0] + " and " + dice[1] + " and landed on square " + players.get(currentPlayer).getLocation());
        }
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
            Monopoly m = (Monopoly) o.readObject();
            this.players = m.players;
            this.currentPlayer = m.currentPlayer;
            this.squares = m.squares;
            // TODO: add all other fields
        }
    }
}