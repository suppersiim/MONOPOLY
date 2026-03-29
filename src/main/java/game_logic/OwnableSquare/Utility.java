package game_logic.OwnableSquare;

import game_logic.Player;

public class Utility extends OwnableSquare{

    private int numberOfUtilitiesOwned;

    public Utility(int rent, Player owner) {
        super(rent, owner);
    }

    @Override
    public int calculateRent() {
        if (numberOfUtilitiesOwned == 1) return 4; // here should be 4 * getDiceRollResult()!!
        else if (numberOfUtilitiesOwned == 2) return 10; // here should be 10 * getDiceRollResult()!!
        else return 0;
    }

}
