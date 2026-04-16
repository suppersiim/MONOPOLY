package game_logic.OwnableSquare;

import game_logic.Player;
import server.GameManager;

public class Utility extends OwnableSquare{

    private int numberOfUtilitiesOwned;

    public Utility(int rent, int price, String name) {
        super(rent, price, name);
    }

    @Override
    public int calculateRent() {
        int[] dice = GameManager.getInstance().getGame().getDice();
        int diceRoll = dice[0] + dice[1];
        if (getOwner().utilitiesOwned().size() == 1) return 4 * diceRoll;
        else if (getOwner().utilitiesOwned().size() == 2) return 10 * diceRoll;
        else return 0;
    }

    @Override
    public void landOn(Player player) {
        super.landOn(player);
    }
}
