package game_logic.NonOwnableSquare;

import game_logic.Player;

public class Jail extends NonOwnableSquare {

    public Jail() {
        super("Jail");
    }

    @Override
    public void landOn(Player player) {
        super.landOn(player);
        // When a player lands on the Jail square, they are just visiting and do not go to jail.
        // No action is needed here.
    }
}

