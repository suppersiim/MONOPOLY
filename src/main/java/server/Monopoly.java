package server;

import game_logic.MonopolyData;
import game_logic.Player;
import game_logic.Square;

import java.util.List;

public class Monopoly extends MonopolyData {
    public Monopoly(List<Player> players) {
        super(players);
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
                    players.get(currentPlayer).goToJail();
                }
            }
            players.get(currentPlayer).move(dice[0] + dice[1]);

            Square squareCurrent = landedOn(players.get(currentPlayer).getLocation());
            System.out.println("Player " + players.get(currentPlayer).getName() + " rolled " + dice[0] + " and " + dice[1] + " and landed on square " + players.get(currentPlayer).getLocation());
        }
    }
}
