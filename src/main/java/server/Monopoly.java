package server;

import game_logic.GameState;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.Utility;
import game_logic.Player;
import game_logic.Square;

import java.util.List;

public class Monopoly extends GameState {
    private int[] doublesCount;


    public Monopoly(List<Player> players) {
        super(players);
        doublesCount = new int[players.size()];
    }



    public int[] diceRoll(){
        dice[0] = (int)(Math.random()*6+1);
        dice[1] = (int)(Math.random()*6+1);
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
            square.landOn(player);

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
