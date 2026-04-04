package game_logic.NonOwnableSquare;

import game_logic.Player;

public class Tax extends NonOwnableSquare {

    @Override
    public void landOn(Player player, int squareIndex) {
        // player pays tax amount
        player.payTax();
    }
}
