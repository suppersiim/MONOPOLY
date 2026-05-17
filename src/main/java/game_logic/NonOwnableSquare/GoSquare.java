package game_logic.NonOwnableSquare;

import game_logic.Player;

public class GoSquare extends NonOwnableSquare {

    public GoSquare() {
        super("Go");
    }

    @Override
    public void landOn(Player player) {
        super.landOn(player);
        // $200 is handled by move() when passing Go
    }
}

