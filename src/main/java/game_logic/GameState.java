package game_logic;

import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.Street;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameState implements Serializable {
    public List<Player> players;
    public int currentPlayer;
    public List<Square> squares;
    public int[] dice = new int[2];
    boolean waitingForBuyResponse = false;
    public OwnableSquare pendingPurchase = null;
    public Street pendingHousePurchase = null;
    public AuctionState auctionState = null;

    public int middlePot = 0; // money that is collected when players pay tax and collected when someone lands on free parking

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

    public Street getPendingHousePurchase() {
        return pendingHousePurchase;
    }

    public void setWaitingForBuyResponse(boolean waitingForBuyResponse) {
        this.waitingForBuyResponse = waitingForBuyResponse;
    }

    public void setPendingPurchase(OwnableSquare pendingPurchase) {
        this.pendingPurchase = pendingPurchase;
    }

    public void setPendingHousePurchase(Street pendingHousePurchase) {
        this.pendingHousePurchase = pendingHousePurchase;
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

    public void addMiddlePot(int tax){
        middlePot += tax;
    }

    public int getMiddlePot() {
        return middlePot;
    }

    public void setMiddlePot(int middlePot) {
        this.middlePot = middlePot;
    }

    public int getPlayerIndexByName(String name) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getName().equals(name)) return i;
        }
        return -1;
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
            this.waitingForBuyResponse = m.waitingForBuyResponse;
            this.pendingPurchase = m.pendingPurchase;
            this.pendingHousePurchase = m.pendingHousePurchase;
            this.middlePot = m.middlePot;
            this.auctionState = m.auctionState;
            // TODO: add all other fields
        }
    }

    public Street findStreetByName(String streetName) {
        for (Square square : squares) {
            if (square instanceof Street street && street.getName().equals(streetName)) {
                return street;
            }
        }
        return null;
    }

    public int getSquareIndexByName(String name) {
        for (int i = 0; i < squares.size(); i++) {
            if (squares.get(i).getName().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public static class AuctionState implements Serializable {
        public String propertyName;
        public int propertySquareIndex;
        public Map<String, Integer> bids = new HashMap<>();
        public List<String> passed = new ArrayList<>();
        public int currentBidderIndex;
        public int startingPlayerIndex;

        public AuctionState(String propertyName, int propertySquareIndex, int startingPlayerIndex) {
            this.propertyName = propertyName;
            this.propertySquareIndex = propertySquareIndex;
            this.startingPlayerIndex = startingPlayerIndex;
            this.currentBidderIndex = startingPlayerIndex;
        }

        public int getHighestBid() {
            return bids.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        }

        public String getHighestBidder() {
            return bids.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse(null);
        }
    }
}