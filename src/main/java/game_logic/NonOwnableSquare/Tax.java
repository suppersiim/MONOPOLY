package game_logic.NonOwnableSquare;

import game_logic.Player;

public class Tax extends NonOwnableSquare {

    private int taxAmount = 200;

    public int getTaxAmount() {
        return taxAmount;
    }

    @Override
    public void landOn(Player player) {
        // player pays tax amount
    }
}
