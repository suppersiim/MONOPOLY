package game_logic.NonOwnableSquare;

import game_logic.Player;

public class GoSquare extends NonOwnableSquare {

    public GoSquare() {
        super("Go");
    }

    @Override
    public void landOn(Player player) {
        // the player recieves 200$ when they land on or pass Go
        player.addMoney(200);
    }
}

