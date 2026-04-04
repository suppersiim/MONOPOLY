package game_logic.NonOwnableSquare;

import game_logic.Player;

public class Tax extends NonOwnableSquare {

    private int taxAmount;

    public int getTax() {
        return taxAmount;
    }

    public Tax(int taxAmount) {
        this.taxAmount = taxAmount;
    }

    @Override
    public void landOn(Player player) {
        // player pays tax amount
        player.payTax(getTax());
    }
}
