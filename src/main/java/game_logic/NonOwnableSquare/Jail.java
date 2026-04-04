package game_logic.NonOwnableSquare;

import game_logic.Player;

public class Jail extends NonOwnableSquare {

    @Override
    public void landOn(Player player) {
        // When a player lands on the Jail square, they are just visiting and do not go to jail.
        // No action is needed here.
    }
}
