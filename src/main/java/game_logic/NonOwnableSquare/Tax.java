package game_logic.NonOwnableSquare;

import game_logic.Player;
import server.GameManager;

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
        // add the money to the middle
        GameManager.getInstance().getGame().addMiddlePot(getTax());
    }
}
