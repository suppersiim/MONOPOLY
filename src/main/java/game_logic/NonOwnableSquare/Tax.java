package game_logic.NonOwnableSquare;

import game_logic.Player;

public class Tax extends NonOwnableSquare {

    private int taxAmount;

    public int getTax() {
        return taxAmount;
    }

    public Tax(int taxAmount) {
        super("Tax");
        this.taxAmount = taxAmount;
    }

    @Override
    public void landOn(Player player) {
        super.landOn(player);
        // player pays tax amount
        player.payMoney(getTax());
    }
}
