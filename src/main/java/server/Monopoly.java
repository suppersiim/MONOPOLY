package server;

import game_logic.GameState;
import game_logic.OwnableSquare.Street;
import game_logic.Player;
import game_logic.Square;
import java.util.List;

public class Monopoly extends GameState {
    private int[] doublesCount;
    private boolean waitingForEndTurn = false;
    public boolean isWaitingForEndTurn() { return waitingForEndTurn; }
    public void setWaitingForEndTurn(boolean v) { this.waitingForEndTurn = v; }


    public Monopoly(List<Player> players) {
        super(players);
        doublesCount = new int[players.size()];
    }

    public void endTurn() {
        waitingForEndTurn = false;
        currentPlayer = (currentPlayer + 1) % players.size();
    }

    public int[] diceRoll(){
        dice[0] = (int)(Math.random()*6+1);
        dice[1] = (int)(Math.random()*6+1);

        String event = players.get(currentPlayer).getName() + " rolled a " + dice[0] + " and a " + dice[1];
        if (dice[0] == dice[1]) {
            event += " (doubles)";
        }
        GameManager.getInstance().broadcastEvent(event);

        return dice;
    }

    public void resolveBuy(boolean accepted) {
        Player player = players.get(currentPlayer);
        if (accepted && getPendingPurchase() != null && player.getMoney() >= getPendingPurchase().getPrice()) {
            player.buy(getPendingPurchase());
        }
        // TODO: auction if declined
        setPendingPurchase(null);
        setWaitingForBuyResponse(false);
        waitingForEndTurn = true;
    }

    public void resolveBuyHouse(boolean accepted) {
        Player player = players.get(currentPlayer);
        Street street = (Street) getPendingPurchase();
        if (accepted && street != null && player.getMoney() >= street.getHousePrice()) {
            player.buyHouse(street);
        }

        setPendingHousePurchase(null);
        //currentPlayer = (currentPlayer + 1) % players.size();
    }

    public void onTurn(){
        Player player = players.get(currentPlayer);
        int[] dice = diceRoll();
        waitingForEndTurn = false;

        if (player.isInJail()) {
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

            if (player.isInJail()){
                doublesCount[currentPlayer] = 0;
                waitingForEndTurn = true;
                return;
            }

            if (dice[0] == dice[1]){
                GameManager.getInstance().broadcastEvent(player.getName() + " rolled doubles — rolls again!");
            }
            else {
                waitingForEndTurn = true;
            }
        }

    }
}
