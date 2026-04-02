package game_logic.NonOwnableSquare;

import game_logic.Player;

public class FreeParking extends NonOwnableSquare {

    @Override
    public void landOn(Player player) {
        // All the collected money is given to the player that landed on that square
        // nothing happens when you step on the square
    }
}
