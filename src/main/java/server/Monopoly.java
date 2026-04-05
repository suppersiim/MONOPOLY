package server;

import game_logic.GameState;
import game_logic.Player;
import game_logic.Square;

import java.util.List;

public class Monopoly extends GameState {
    public Monopoly(List<Player> players) {
        super(players);
    }

    public int[] diceRoll(){
        dice[0] = (int)(Math.random()*6+1);
        dice[1] = (int)(Math.random()*6+1);
        return dice;
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

            Square squareCurrent = getSquare(players.get(currentPlayer).getLocation());
            System.out.println("Player " + players.get(currentPlayer).getName() + " rolled " + dice[0] + " and " + dice[1] + " and landed on square " + players.get(currentPlayer).getLocation());
        }
        currentPlayer = (currentPlayer + 1) % players.size();
    }
}
