package server;

import game_logic.GameState;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.Street;
import game_logic.Player;
import game_logic.Square;
import java.util.List;
import game_logic.GameState.AuctionState;
import game_logic.OwnableSquare.OwnableSquare;

public class Monopoly extends GameState {
    private int[] doublesCount;
    private int[] turnsInJail;


    public Monopoly(List<Player> players) {
        super(players);
        doublesCount = new int[players.size()];
        turnsInJail = new int[players.size()];
    }


    public boolean isDouble() {
        return dice[0] == dice[1];
    }

    public int[] diceRoll(){
        dice[0] = (int)(Math.random()*6+1);
        dice[1] = (int)(Math.random()*6+1);

        String event = players.get(currentPlayer).getName() + " rolled a " + dice[0] + " and a " + dice[1];
        if (isDouble()) {
            event += " (doubles)";
        }
        GameManager.getInstance().broadcastEvent(event);

        return dice;
    }

    public void resolveBuy(boolean accepted) {
        Player player = players.get(currentPlayer);
        if (accepted && getPendingPurchase() != null && player.getMoney() >= getPendingPurchase().getPrice()) {
            player.buy(getPendingPurchase());
            setPendingPurchase(null);
            setWaitingForBuyResponse(false);
        } else {
            OwnableSquare property = getPendingPurchase();
            setPendingPurchase(null);
            setWaitingForBuyResponse(false);
            startAuction(property);
        }
        setPendingPurchase(null);
        setWaitingForBuyResponse(false);
    }

    private void startAuction(OwnableSquare property) {
        int squareIndex = getSquareIndexByName(property.getName());
        auctionState = new AuctionState(property.getName(), squareIndex, currentPlayer);
        for (Player p : players) {
            auctionState.bids.put(p.getName(), 0);
        }
        GameManager.getInstance().broadcastEvent("Auction started for " + property.getName() + "! Starting with " + players.get(currentPlayer).getName());
        GameManager.getInstance().broadcastAuctionState();
    }

    public void resolveAuctionBid(String playerName, int bidAmount) {
        if (auctionState == null) return;
        Player bidder = getPlayerByName(playerName);
        if (bidder == null) return;
        int highestBid = auctionState.getHighestBid();
        if (bidAmount <= highestBid || bidAmount > bidder.getMoney()) return;
        auctionState.bids.put(playerName, bidAmount);
        GameManager.getInstance().broadcastEvent(playerName + " bid $" + bidAmount + " on " + auctionState.propertyName);
        advanceAuctionTurn();
    }

    public void resolveAuctionPass(String playerName) {
        if (auctionState == null) return;
        if (!auctionState.passed.contains(playerName)) {
            auctionState.passed.add(playerName);
        }
        GameManager.getInstance().broadcastEvent(playerName + " passed on " + auctionState.propertyName);
        advanceAuctionTurn();
    }

    private void advanceAuctionTurn() {
        long activePlayers = players.stream()
                .filter(p -> !auctionState.passed.contains(p.getName()))
                .count();
        if (activePlayers <= 1) {
            finishAuction();
            return;
        }
        int next = (auctionState.currentBidderIndex + 1) % players.size();
        while (auctionState.passed.contains(players.get(next).getName())) {
            next = (next + 1) % players.size();
        }
        auctionState.currentBidderIndex = next;
        GameManager.getInstance().broadcastAuctionState();
    }

    private void finishAuction() {
        String winner = auctionState.getHighestBidder();
        int winningBid = auctionState.getHighestBid();
        OwnableSquare property = (OwnableSquare) squares.get(auctionState.propertySquareIndex);
        if (winner != null && winningBid > 0) {
            Player winnerPlayer = getPlayerByName(winner);
            winnerPlayer.buy(property);
            winnerPlayer.addMoney(property.getPrice() - winningBid);
            GameManager.getInstance().broadcastEvent(winner + " won the auction for " + auctionState.propertyName + " with a bid of $" + winningBid + "!");
        } else {
            GameManager.getInstance().broadcastEvent("No bids placed — " + auctionState.propertyName + " remains unowned.");
        }
        auctionState = null;
        GameManager.getInstance().broadcastGameState();
    }

    public void resolveBuyHouse(boolean accepted) {
        Player player = players.get(currentPlayer);
        Street street = (Street) getPendingPurchase();
        if (accepted && street != null && player.getMoney() >= street.getHousePrice() && street.getNumberOfHouses() < 5) {
            player.buyHouse(street);
        }

        setPendingHousePurchase(null);
    }

    public void eliminatePlayer(Player player) {
        // return properties to bank
        for (OwnableSquare property : player.getProperties()) {
            property.setOwner(null);
            property.setMortgaged(false);
            if (property instanceof Street street) {
                while (street.getNumberOfHouses() > 0) street.removeHouse();
            }
        }
        players.remove(player);
        GameManager.getInstance().broadcastEvent(player.getName() + " is bankrupt and has been eliminated!");
    }

    public void advanceTurn() {
        if (getCurrentPlayer().hasRolled()) {
            getCurrentPlayer().setHasRolled(false);
            currentPlayer = (currentPlayer + 1) % players.size();
        }
    }

    public void rollAndSkipCardCheck() {
        Player player = players.get(currentPlayer);
        if (player.hasRolled()) return;

        int[] dice = diceRoll();

        if (player.isInJail()) {
            if (isDouble()) {
                player.setInJail(false);
                doublesCount[currentPlayer] = 0;
                turnsInJail[currentPlayer] = 0;
                player.setHasRolled(true);
            } else {
                turnsInJail[currentPlayer] += 1;
                if (turnsInJail[currentPlayer] == 3) {
                    player.setInJail(false);
                    doublesCount[currentPlayer] = 0;
                    turnsInJail[currentPlayer] = 0;
                    player.setHasRolled(true);
                }
            }
        }
    }

    public void onTurn(){

        Player player = players.get(currentPlayer);
        if (getCurrentPlayer().hasRolled()) {
            return;
        }

        // check for jail card BEFORE rolling
        if (player.isInJail() && player.hasGetOutOfJailCard() && !isWaitingForJailCardResponse()) {
            setWaitingForJailCardResponse(true);
            return; // pause and wait for player response
        }

        int[] dice = diceRoll();
        if (player.isInJail()) {
            if (isDouble()){
                player.setInJail(false);
                doublesCount[currentPlayer] = 0;
                turnsInJail[currentPlayer] = 0;
                player.setHasRolled(true);
            }
            else {
                turnsInJail[currentPlayer] += 1;
                if (turnsInJail[currentPlayer] == 3) {
                    player.setInJail(false);
                    doublesCount[currentPlayer] = 0;
                    turnsInJail[currentPlayer] = 0;
                    player.setHasRolled(true);
                }
            }
        }
        else {
            if (isDouble()) {
                doublesCount[currentPlayer] += 1;
                if (doublesCount[currentPlayer] == 3) {
                    player.goToJail();
                    doublesCount[currentPlayer] = 0;
                    advanceTurn();
                    return;
                }
            } else {
                doublesCount[currentPlayer] = 0;
            }

            player.move(dice[0] + dice[1]);

            Square square = getSquare(player.getLocation());
            square.landOn(player);

            if (player.isInJail()) {
                if (player.hasGetOutOfJailCard() && !isWaitingForJailCardResponse()) {
                    setWaitingForJailCardResponse(true);
                    return;
                }
                doublesCount[currentPlayer] = 0;
                return;
            }

            if (isDouble()){
                player.setHasRolled(false);
                System.out.println(player.getName() + " rolled doubles -- rolls again!");
            }
            else {
                player.setHasRolled(true);
            }
        }
    }
}
