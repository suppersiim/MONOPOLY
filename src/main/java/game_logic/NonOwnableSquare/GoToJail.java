package game_logic.NonOwnableSquare;

import game_logic.Player;

public class GoToJail extends NonOwnableSquare {
    @Override
    public void landOn(Player player) {
        // Move the player to the Jail square
        player.goToJail();
    }
}
