package game_logic;

import game_logic.NonOwnableSquare.*;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.RailRoad;
import game_logic.OwnableSquare.Street;
import game_logic.OwnableSquare.Utility;

import java.io.*;
import java.util.List;
import javafx.scene.paint.Color;

public class GameState implements Serializable {
    public List<Player> players;
    public int currentPlayer;
    private List<Square> squares;
    public int[] dice = new int[2];
    private boolean waitingForBuyResponse = false;
    private OwnableSquare pendingPurchase = null;

    public GameState(List<Player> players) {
        this.players = players;
        this.currentPlayer = 0;
        BoardLoader boardLoader = new BoardLoader();
        this.squares = boardLoader.loadBoard();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayer);
    }

    public boolean isWaitingForBuyResponse() {
        return waitingForBuyResponse;
    }
    public OwnableSquare getPendingPurchase() {
        return pendingPurchase;
    }

    public void setWaitingForBuyResponse(boolean waitingForBuyResponse) {
        this.waitingForBuyResponse = waitingForBuyResponse;
    }

    public void setPendingPurchase(OwnableSquare pendingPurchase) {
        this.pendingPurchase = pendingPurchase;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayer;
    }
    
    public Player getPlayerByName(String name) {
        for (Player p : players) {
            if (p.getName().equals(name)) return p;
        }
        return null;
    }

    public int[] getDice() {
        return dice;
    }

    public List<Square> getSquares() {
        return squares;
    }

    public Square getSquare(int location) {
        return squares.get(location);
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
            GameState m = (GameState) o.readObject();
            this.players = m.players;
            this.currentPlayer = m.currentPlayer;
            this.squares = m.squares;
            this.dice = m.dice;
            // TODO: add all other fields
        }
    }
}