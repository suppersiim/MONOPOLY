package server;

import game_logic.GameState;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.Utility;
import game_logic.Player;
import game_logic.Square;

import java.util.List;

public class Monopoly extends GameState {
    private int[] doublesCount;
    private boolean waitingForBuyResponse = false;
    private OwnableSquare pendingPurchase = null;

    public Monopoly(List<Player> players) {
        super(players);
        doublesCount = new int[players.size()];
    }

    public boolean isWaitingForBuyResponse() {
        return waitingForBuyResponse;
    }

    public OwnableSquare getPendingPurchase() {
        return pendingPurchase;
    }

    public int[] diceRoll(){
        dice[0] = (int)(Math.random()*6+1);
        dice[1] = (int)(Math.random()*6+1);
        return dice;
    }

    public void resolveBuy(boolean accepted) {
        Player player = players.get(currentPlayer);
        if (accepted && pendingPurchase != null && player.getMoney() >= pendingPurchase.getPrice()) {
            player.buy(pendingPurchase);
        }
        // TODO: auction if declined
        pendingPurchase = null;
        waitingForBuyResponse = false;
        currentPlayer = (currentPlayer + 1) % players.size();
    }

    public void onTurn(){
        Player player = players.get(currentPlayer);
        if (player.isInJail()) {
            int[] dice = diceRoll();
            if (dice[0]==dice[1]){
                player.setInJail(false);
                doublesCount[currentPlayer] = 0;
                currentPlayer = (currentPlayer + 1) % players.size();
            }
            else {
                doublesCount[currentPlayer] += 1;
                if (doublesCount[currentPlayer] == 3) {
                    player.setInJail(false);
                    doublesCount[currentPlayer] = 0;
                    currentPlayer = (currentPlayer + 1) % players.size();
                }
            }
        }
        else {
            int[] dice = diceRoll();
            if (dice[0] == dice[1]) {
                doublesCount[currentPlayer] += 1;
                if (doublesCount[currentPlayer] == 3) {
                    player.goToJail();
                    doublesCount[currentPlayer] = 0;
                    currentPlayer = (currentPlayer + 1) % players.size();
                    return;
                }
            }

            player.move(dice[0] + dice[1]);

            Square square = getSquare(player.getLocation());
            System.out.println(player.getName() + " landed on: " + square.getName() + " (position " + player.getLocation() + ")");
            if (square instanceof OwnableSquare ownable && ownable.getOwner() == null) {
                pendingPurchase = ownable;
                waitingForBuyResponse = true;
                System.out.println(player.getName() + " can buy " + ownable.getName() + " for $" + ownable.getPrice() + ".");
            }
            else square.landOn(player);

            if (player.isInJail()){
                doublesCount[currentPlayer] = 0;
                currentPlayer = (currentPlayer + 1) % players.size();
                return;
            }

            if (dice[0] == dice[1]){
                System.out.println(player.getName() + " rolled doubles -- rolls again!");
            }
            else {
                currentPlayer = (currentPlayer + 1) % players.size();
            }
        }

    }
}
