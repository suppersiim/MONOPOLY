package game_logic.NonOwnableSquare;

import game_logic.Player;

public class Tax extends NonOwnableSquare {

    private int tax;

    public int getTax() {
        return tax;
    }

    public Tax(int tax) {
        this.tax = tax;
    }

    @Override
    public void landOn(Player player, int squareIndex) {
        // player pays tax amount
        player.payTax(getTax());
    }
}
