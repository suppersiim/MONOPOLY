package game_logic.NonOwnableSquare;

import game_logic.Player;

public class GoSquare extends NonOwnableSquare {

    @Override
    public void landOn(Player player, int squareIndex) {
        // the player recieves 200$ when they land on or pass Go
        player.addMoney(200);
    }
}
