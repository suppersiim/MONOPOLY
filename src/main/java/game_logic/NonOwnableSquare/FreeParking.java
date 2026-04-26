package game_logic.NonOwnableSquare;

import game_logic.Player;
import server.GameManager;

public class FreeParking extends NonOwnableSquare {

    public FreeParking() {
        super("Free Parking");
    }

    @Override
    public void landOn(Player player) {
        super.landOn(player);

        // All the collected money is given to the player that landed on that square
        int pot = GameManager.getInstance().getGame().getMiddlePot();
        if (pot > 0) {
            player.addMoney(pot);
            GameManager.getInstance().getGame().setMiddlePot(0);
            GameManager.getInstance().broadcastEvent(player.getName() + " collected $" + pot + " from Free Parking!");
        }
    }
}

